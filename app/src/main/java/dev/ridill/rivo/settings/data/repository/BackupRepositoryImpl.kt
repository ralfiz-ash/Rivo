package dev.ridill.rivo.settings.data.repository

import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.gson.Gson
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.rivo.core.data.util.tryNetworkCall
import dev.ridill.rivo.core.domain.model.DataError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.remote.GDriveApi
import dev.ridill.rivo.settings.data.remote.MEDIA_PART_KEY
import dev.ridill.rivo.settings.data.remote.dto.CreateGDriveFolderRequestDto
import dev.ridill.rivo.settings.data.toBackupDetails
import dev.ridill.rivo.settings.domain.backup.BackupCachingFailedThrowable
import dev.ridill.rivo.settings.domain.backup.BackupService
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.backup.DB_BACKUP_FILE_NAME
import dev.ridill.rivo.settings.domain.backup.RestoreFailedThrowable
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import dev.ridill.rivo.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalDateTime
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

class BackupRepositoryImpl(
    private val backupService: BackupService,
    private val gDriveApi: GDriveApi,
    private val preferencesManager: PreferencesManager,
    private val securityPreferencesManager: SecurityPreferencesManager,
    private val configDao: ConfigDao,
    private val backupWorkManager: BackupWorkManager,
    private val schedulesRepository: SchedulesRepository,
    private val authRepo: AuthRepository
) : BackupRepository {
    override suspend fun checkForBackup(): Result<BackupDetails, DataError> =
        tryNetworkCall {
            logI { "Checking For Backup" }
            val email = authRepo.getSignedInAccount()?.email
                ?: throw GoogleAuthException()
            val backupFolderName = backupFolderName(email)
            val backupFolder = gDriveApi.getFilesList(
                q = "trashed=false and name = '$backupFolderName'"
            ).files.firstOrNull()
                ?: throw NoBackupFoundThrowable()
            logD { "Backup folder - $backupFolder" }
            val backupFile = gDriveApi.getFilesList(
                q = "trashed=false and '${backupFolder.id}' in parents and name contains '$DB_BACKUP_FILE_NAME'",
            ).files.firstOrNull()
                ?: throw NoBackupFoundThrowable()

            val backupDetails = backupFile.toBackupDetails()
            logD { "Backup Found - $backupDetails" }
            Result.Success(backupDetails)
        }

    @Throws(
        InvalidEncryptionPasswordThrowable::class,
        BackupCachingFailedThrowable::class,
        IOException::class,
        UserRecoverableAuthException::class,
        GoogleAuthException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    override suspend fun performAppDataBackup() = withContext(Dispatchers.IO) {
        logI { "Performing Data Backup" }
        val securityPreferences = securityPreferencesManager.preferences.first()
        val passwordHash = securityPreferences
            .backupEncryptionHash.orEmpty()
            .ifEmpty { throw InvalidEncryptionPasswordThrowable() }
        val passwordHashSalt = securityPreferences
            .backupEncryptionHashSalt.orEmpty()
            .ifEmpty { throw InvalidEncryptionPasswordThrowable() }
        val email = authRepo.getSignedInAccount()?.email
            ?: throw GoogleAuthException()
        val backupFolderName = backupFolderName(email)
        var backupFolder = gDriveApi.getFilesList(
            q = "name = '$backupFolderName' and trashed=false"
        ).files.firstOrNull()
        logD { "Received backup folder - $backupFolder" }
        if (backupFolder == null) {
            val createBackupFolderRequest = CreateGDriveFolderRequestDto(backupFolderName(email))
            val createBackupFolderMetadataPart = Gson().toJson(createBackupFolderRequest)
                .toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())
            logD { "Create backup folder metadata - $createBackupFolderMetadataPart" }
            backupFolder = gDriveApi.createFolder(createBackupFolderMetadataPart)
        }
        val backupFile = backupService.buildBackupFile(
            password = passwordHash,
            passwordSalt = passwordHashSalt
        )
        val metadataMap = mapOf(
            "name" to backupFile.name,
            "parents" to listOf(backupFolder.id)
        )
        val metadataJson = Gson().toJson(metadataMap)
        val metadataPart = metadataJson.toRequestBody(JSON_MIME_TYPE.toMediaTypeOrNull())

        val fileBody = backupFile.asRequestBody(BACKUP_MIME_TYPE.toMediaTypeOrNull())
        val mediaPart = MultipartBody.Part.createFormData(
            MEDIA_PART_KEY,
            backupFile.name,
            fileBody
        )

        logD { "Backup file generated - ${backupFile.name}" }
        val gDriveBackup = gDriveApi.uploadFile(
            metadata = metadataPart,
            file = mediaPart
        )
        logI { "Backup file uploaded - $gDriveBackup" }

        preferencesManager.updateLastBackupTimestamp(DateUtil.now())
        val backupFolderFiles = gDriveApi.getFilesList(
            q = "trashed=false and '${backupFolder.id}' in parents"
        ).files
        for (file in backupFolderFiles) {
            if (file.id == gDriveBackup.id) continue
            gDriveApi.deleteFile(file.id)
        }
        logI { "Cleaned up Drive" }
    }

    @Throws(
        RestoreFailedThrowable::class,
        BackupDownloadFailedThrowable::class,
        BackupCachingFailedThrowable::class
    )
    override suspend fun downloadAndCacheBackupData(
        fileId: String,
        timestamp: LocalDateTime
    ) = withContext(Dispatchers.IO) {
        if (backupService.doesRestoreCacheExist(timestamp)) {
            logI { "Cache already exists, hence skipping download" }
            return@withContext
        }

        logI { "Downloading data from GDrive" }
        val response = gDriveApi.downloadFile(fileId)
        val fileBody = response.body()
            ?: throw BackupDownloadFailedThrowable()
        logI { "Downloaded backup data" }
        backupService.cacheDownloadedRestoreData(fileBody.byteStream(), timestamp)
        logI { "Cached restore data" }
    }

    @Throws(
        RestoreFailedThrowable::class,
        BackupDownloadFailedThrowable::class,
        BackupCachingFailedThrowable::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    override suspend fun performAppDataRestoreFromCache(
        passwordHash: String,
        passwordSalt: String,
        timestamp: LocalDateTime
    ) = withContext(Dispatchers.IO) {
        logI { "Restoring Backup from cache" }
        backupService.restoreBackupFromCache(
            password = passwordHash,
            passwordSalt = passwordSalt,
            timestamp = timestamp
        )
        securityPreferencesManager.updateBackupEncryptionHash(
            hash = passwordHash,
            salt = passwordSalt
        )
        preferencesManager.updateLastBackupTimestamp(timestamp)
        logI { "Updated last backup timestamp" }
    }

    override suspend fun tryClearLocalCache() {
        tryOrNull("Clearing cacheDir exception") {
            backupService.clearCache()
        }
    }

    private fun backupFolderName(email: String): String = "Rivo $email backup"

    override suspend fun setBackupError(error: FatalBackupError?) =
        preferencesManager.updateFatalBackupError(error)

    override suspend fun restoreAppConfig() = withContext(Dispatchers.IO) {
        // Schedule backup job
        scheduleBackupJob()

        // Set reminders
        setReminders()
    }

    private suspend fun scheduleBackupJob() {
        val backupInterval = configDao.getBackupInterval()
            ?.let { BackupInterval.valueOf(it) }
            ?: return
        if (backupInterval == BackupInterval.MANUAL) return
        backupWorkManager.schedulePeriodicBackupWork(backupInterval)
    }

    private suspend fun setReminders() {
        schedulesRepository.setAllFutureScheduleReminders()
    }
}

const val JSON_MIME_TYPE = "application/json"
const val BACKUP_MIME_TYPE = "application/octet-stream"
const val APP_DATA_SPACE = "appDataFolder"
const val G_DRIVE_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
//const val G_DRIVE_SHORTCUT_MIME_TYPE = "application/vnd.google-apps.shortcut"

class NoBackupFoundThrowable : Throwable("No Backups Found")
class BackupDownloadFailedThrowable : Throwable("Failed to download backup data")
class InvalidEncryptionPasswordThrowable : Throwable("")