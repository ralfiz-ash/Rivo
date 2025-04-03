package dev.ridill.rivo.settings.data

import dev.ridill.rivo.settings.data.remote.GDriveApi
import dev.ridill.rivo.settings.data.remote.dto.GDriveFileDto
import dev.ridill.rivo.settings.domain.modal.BackupDetails

fun GDriveFileDto.toBackupDetails(): BackupDetails = BackupDetails(
    name = name,
    id = id,
    timestamp = (appProperties[GDriveApi.APP_PROPERTIES_KEY_BACKUP_TIMESTAMP] as? String).orEmpty(),
    hashSalt = appProperties[GDriveApi.APP_PROPERTIES_KEY_HASH_SALT] as? String?
)