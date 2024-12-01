package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.repository.SchedulesRepositoryImpl
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.notification.ScheduleReminderNotificationHelper
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.schedules.domain.scheduleReminder.AlarmManagerScheduleReminder
import dev.ridill.rivo.schedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao

@Module
@InstallIn(SingletonComponent::class)
object SchedulesSingletonModule {
    @Provides
    fun provideSchedulesDao(database: RivoDatabase): SchedulesDao =
        database.schedulesDao()

    @Provides
    fun provideSchedulesRepository(
        db: RivoDatabase,
        schedulesDao: SchedulesDao,
        transactionDao: TransactionDao,
        scheduler: ScheduleReminder,
        receiverService: ReceiverService,
        currencyRepo: CurrencyRepository
    ): SchedulesRepository = SchedulesRepositoryImpl(
        db = db,
        schedulesDao = schedulesDao,
        transactionDao = transactionDao,
        scheduler = scheduler,
        receiverService = receiverService,
        currencyRepo = currencyRepo
    )

    @Provides
    fun provideScheduleReminder(
        @ApplicationContext context: Context
    ): ScheduleReminder = AlarmManagerScheduleReminder(context)

    @Provides
    fun provideScheduleReminderNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Schedule> = ScheduleReminderNotificationHelper(context)
}