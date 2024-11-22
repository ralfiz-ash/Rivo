package dev.ridill.rivo.settings.domain.backup

import android.content.Context
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.domain.util.toByteArray
import dev.ridill.rivo.core.domain.util.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

class BackupService(
    private val context: Context,
    private val database: RivoDatabase,
    private val cryptoManager: CryptoManager
) {
    @Throws(
        RestoreFailedThrowable::class,
        BackupCachingFailedThrowable::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    suspend fun restoreBackupFromCache(
        password: String,
        timestamp: LocalDateTime
    ) = withContext(Dispatchers.IO) {
        val dbPath = database.openHelper.readableDatabase.path
        val dbFile = dbPath?.let { File(it) } ?: throw BackupCachingFailedThrowable()
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val cachePath = context.externalCacheDir ?: throw BackupCachingFailedThrowable()
        logI { "Create decrypted cache" }
        val decryptedDataCache = File(cachePath, DB_TEMP_CACHE_FILENAME)

        val encryptedDataCache = File(cachePath, buildRestoreCacheFileName(timestamp))
        if (!encryptedDataCache.exists()) throw RestoreFailedThrowable()

        encryptedDataCache.inputStream().use encryptedDataCacheInputStream@{ inputStream ->
            val ivSizeBytes = ByteArray(Int.SIZE_BYTES)
            inputStream.read(ivSizeBytes)
            val ivSize = ivSizeBytes.toInt()

            logI { "Read iv data" }
            var ivBytesLeft = ivSize
            var ivBytes = ByteArray(0)
            while (ivBytesLeft > 0) {
                val data = ByteArray(minOf(DEFAULT_BUFFER_SIZE, ivBytesLeft))
                val bytesRead = inputStream.read(data)
                ivBytes += data.copyOfRange(0, bytesRead)
                ivBytesLeft -= bytesRead
                ensureActive()
            }

            logI { "Read encrypted data" }
            val dataBytes = inputStream.readBytes()
            logI { "Decrypt data" }
            val decryptedBytes = cryptoManager.decrypt(dataBytes, ivBytes, password)

            logI { "Write decrypted data to decrypted cache" }
            decryptedDataCache.outputStream().use decryptedDataCacheOutputStream@{
                decryptedBytes.forEach { byte ->
                    it.write(byte.toInt())
                    ensureActive()
                }
//                it.write(decryptedBytes)
            }
        }

        logI { "Write decrypted cache to DB files" }
        decryptedDataCache.inputStream().use decryptedDataCacheInputStream@{ inputStream ->
            // Read DB Data
            dbFile.outputStream().use dbOutputStream@{
                val dbSizeBytes = ByteArray(Int.SIZE_BYTES)
                inputStream.read(dbSizeBytes)
                val dbSize = dbSizeBytes.toInt()

                var bytesLeft = dbSize
                var byteArray = ByteArray(0)
                while (bytesLeft > 0) {
                    val data = ByteArray(minOf(DEFAULT_BUFFER_SIZE, bytesLeft))
                    val bytesRead = inputStream.read(data)
                    byteArray += data.copyOfRange(0, bytesRead)
                    bytesLeft -= bytesRead
                    ensureActive()
                }
                it.write(byteArray)
            }

            // Read WAL Data
            dbWalFile.outputStream().use walOutputStream@{
                val walSizeBytes = ByteArray(Int.SIZE_BYTES)
                val sizeBytesRead = inputStream.read(walSizeBytes)
                if (sizeBytesRead == -1) return@walOutputStream
                val walSize = walSizeBytes.toInt()

                var bytesLeft = walSize
                var byteArray = ByteArray(0)
                while (bytesLeft > 0) {
                    val data = ByteArray(minOf(DEFAULT_BUFFER_SIZE, bytesLeft))
                    val bytesRead = inputStream.read(data)
                    byteArray += data.copyOfRange(0, bytesRead)
                    bytesLeft -= bytesRead
                    ensureActive()
                }
                it.write(byteArray)
            }

            // Read SHM Data
            dbShmFile.outputStream().use shmOutputStream@{
                val shmSizeBytes = ByteArray(Int.SIZE_BYTES)
                val sizeBytesRead = inputStream.read(shmSizeBytes)
                if (sizeBytesRead == -1) return@shmOutputStream
                val shmSize = shmSizeBytes.toInt()

                var bytesLeft = shmSize
                var byteArray = byteArrayOf()
                while (bytesLeft > 0) {
                    val data = ByteArray(minOf(DEFAULT_BUFFER_SIZE, bytesLeft))
                    val bytesRead = inputStream.read(data)
                    byteArray += data.copyOfRange(0, bytesRead)
                    bytesLeft -= bytesRead
                    ensureActive()
                }
                it.write(byteArray)
            }
        }
        checkpointDb()
    }

    @Throws(
        BackupCachingFailedThrowable::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    suspend fun buildBackupFile(
        password: String
    ): File = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(RivoDatabase.NAME)
        val dbWalFile = File(dbFile.path + SQLITE_WAL_FILE_SUFFIX)
        val dbShmFile = File(dbFile.path + SQLITE_SHM_FILE_SUFFIX)

        val cachePath = context.externalCacheDir ?: throw BackupCachingFailedThrowable()
        logI { "Create temp backup cache file" }
        val dbCache = File(cachePath, DB_TEMP_CACHE_FILENAME)
        if (dbCache.exists()) dbCache.delete()

        logI { "Checkpoint DB" }
        checkpointDb()
        logI { "Read DB data into temp backup cache file" }
        dbCache.outputStream().use tempCacheOutputStream@{ outputStream ->
            // Write DB Data
            dbFile.inputStream().use dbInputStream@{
                val dbData = it.readBytes()
                outputStream.write(dbData.size.toByteArray())
                outputStream.write(dbData)
            }

            // Write WAL Data
            if (dbWalFile.exists()) dbWalFile.inputStream().use walInputStream@{
                val walData = it.readBytes()
                outputStream.write(walData.size.toByteArray())
                outputStream.write(walData)
            }

            // Write SHM Data
            if (dbShmFile.exists()) dbShmFile.inputStream().use shmInputStream@{
                val shmData = it.readBytes()
                outputStream.write(shmData.size.toByteArray())
                outputStream.write(shmData)
            }
        }

        logI { "Create DB backup file" }
        val encryptedBackupFile = File(cachePath, backupFileName())
        if (encryptedBackupFile.exists()) encryptedBackupFile.delete()
        dbCache.inputStream().use dbCacheInputStream@{
            val rawBytes = it.readBytes()
            logI { "Encrypt temp backup cache data" }
            val encryptionResult = cryptoManager.encrypt(rawBytes, password)
            encryptedBackupFile.outputStream().use backupFileOutputStream@{ outputStream ->
                logI { "Write encrypted temp backup cache data to backup file" }
                outputStream.write(encryptionResult.iv.size.toByteArray())
                outputStream.write(encryptionResult.iv)
                outputStream.write(encryptionResult.data)
            }
        }

        encryptedBackupFile
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        val cacheDir = context.externalCacheDir
        cacheDir?.delete()
        logI { "Cleared local cacheDir" }
    }

    private fun checkpointDb() {
        val writableDb = database.openHelper.writableDatabase
        writableDb.query("PRAGMA wal_checkpoint(FULL);")
        writableDb.query("PRAGMA wal_checkpoint(TRUNCATE);")
    }

    private fun backupFileName(): String = "${DateUtil.now()}-$DB_BACKUP_FILE_NAME"

    fun doesRestoreCacheExist(timestamp: LocalDateTime): Boolean {
        val cachePath = context.externalCacheDir ?: throw RestoreFailedThrowable()
        val restoreCacheFile = File(cachePath, buildRestoreCacheFileName(timestamp))
        return restoreCacheFile.exists()
    }

    @Throws(RestoreFailedThrowable::class)
    suspend fun cacheDownloadedRestoreData(
        dataStream: InputStream,
        dateTime: LocalDateTime,
        refreshCache: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val cachePath = context.externalCacheDir ?: throw RestoreFailedThrowable()
        logI { "Create Restore Data Cache" }
        val restoreDataCache = File(cachePath, buildRestoreCacheFileName(dateTime))
        if (restoreDataCache.exists() && !refreshCache) return@withContext
        dataStream.use downloadedInputStream@{ inputStream ->
            restoreDataCache.outputStream().use restoreCacheOutputStream@{ outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun buildRestoreCacheFileName(timestamp: LocalDateTime): String =
        "$timestamp-$RESTORE_CACHE_FILE"
}

private const val DB_TEMP_CACHE_FILENAME = "DBBackupCache.backup"
private const val SQLITE_WAL_FILE_SUFFIX = "-wal"
private const val SQLITE_SHM_FILE_SUFFIX = "-shm"
const val DB_BACKUP_FILE_NAME = "Rivo_db.backup"
private const val RESTORE_CACHE_FILE = "RestoreCache.backup"

class BackupCachingFailedThrowable : Throwable("Failed to create backup cache")
class RestoreFailedThrowable : Throwable("Failed to restore data")