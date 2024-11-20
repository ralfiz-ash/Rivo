package dev.ridill.rivo.schedules.domain.scheduleReminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.notification.ScheduleReminderNotificationHelper
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MarkScheduleAsPaidActionReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var repo: SchedulesRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<Schedule>

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ScheduleReminderNotificationHelper.ACTION_MARK_SCHEDULED_AS_PAID)
            return

        applicationScope.launch {
            val scheduleId = intent.getLongExtra(ScheduleReminder.EXTRA_SCHEDULE_ID, -1L)
                .takeIf { it > -1L }
                ?: return@launch
            val schedule = repo.getScheduleById(scheduleId)
                ?: return@launch
            repo.createTransactionFromScheduleAndSetNextReminder(schedule)

            notificationHelper.updateNotification(
                id = scheduleId.hashCode(),
                notification = notificationHelper
                    .buildBaseNotification()
                    .setContentTitle(context?.getString(R.string.schedule_marked_as_paid))
                    .setTimeoutAfter(NotificationHelper.Utils.TIMEOUT_MILLIS)
                    .build()
            )
        }
    }
}