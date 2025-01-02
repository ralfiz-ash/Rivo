package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.repository.BackupSettingsRepositoryImpl
import dev.ridill.rivo.settings.data.repository.SettingsRepositoryImpl
import dev.ridill.rivo.settings.domain.appInit.AppInitWorkManager
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.settings.presentation.backupEncryption.BackupEncryptionViewModel
import dev.ridill.rivo.settings.presentation.backupSettings.BackupSettingsViewModel
import dev.ridill.rivo.settings.presentation.securitySettings.SecuritySettingsViewModel
import dev.ridill.rivo.settings.presentation.settings.SettingsViewModel

@Module
@InstallIn(ViewModelComponent::class)
object SettingsViewModelModule {

    @Provides
    fun provideSettingsRepository(
        authRepo: AuthRepository,
        preferencesManager: PreferencesManager,
        budgetRepo: BudgetPreferenceRepository,
        currencyRepo: CurrencyRepository
    ): SettingsRepository = SettingsRepositoryImpl(
        authRepo = authRepo,
        preferencesManager = preferencesManager,
        budgetRepo = budgetRepo,
        currencyRepo = currencyRepo
    )

    @Provides
    fun provideSettingsEventBus(): EventBus<SettingsViewModel.SettingsEvent> = EventBus()

    @Provides
    fun provideBackupSettingsEventBus(): EventBus<BackupSettingsViewModel.BackupSettingsEvent> =
        EventBus()

    @Provides
    fun provideBackupSettingsRepository(
        dao: ConfigDao,
        preferencesManager: PreferencesManager,
        backupWorkManager: BackupWorkManager,
        cryptoManager: CryptoManager
    ): BackupSettingsRepository = BackupSettingsRepositoryImpl(
        dao = dao,
        preferencesManager = preferencesManager,
        backupWorkManager = backupWorkManager,
        cryptoManager = cryptoManager
    )

    @Provides
    fun provideSecuritySettingsEventBus(): EventBus<SecuritySettingsViewModel.SecuritySettingsEvent> =
        EventBus()

    @Provides
    fun provideBackupEncryptionEventBus(): EventBus<BackupEncryptionViewModel.BackupEncryptionEvent> =
        EventBus()

    @Provides
    fun provideAppInitWorkManager(
        @ApplicationContext context: Context
    ): AppInitWorkManager = AppInitWorkManager(context)
}