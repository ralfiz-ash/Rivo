package dev.ridill.rivo.schedules.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.room.withTransaction
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.domain.util.isSameMonthAs
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.domain.model.ScheduleListItemUiModel
import dev.ridill.rivo.schedules.domain.repository.AllSchedulesRepository
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class AllSchedulesRepositoryImpl(
    private val db: RivoDatabase,
    private val dao: SchedulesDao,
    private val repo: SchedulesRepository
) : AllSchedulesRepository {
    private val currentDate = MutableStateFlow(DateUtil.dateNow())

    override fun refreshCurrentDate() {
        currentDate.update { DateUtil.dateNow() }
    }

    override fun getSchedulesPagingData(): Flow<PagingData<ScheduleListItemUiModel>> = Pager(
        config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = { dao.getSchedulesPaged() }
    ).flow
        .combine(currentDate) { pagingData, dateNow ->
            pagingData
                .map {
                    ScheduleListItemUiModel.ScheduleItem(
                        scheduleItem = it,
                        canMarkPaid = it.nextReminderTimestamp?.isSameMonthAs(dateNow) == true
                    )
                }
                .insertSeparators<ScheduleListItemUiModel.ScheduleItem, ScheduleListItemUiModel>
                { before, after ->
                    when {
                        before?.nextReminderTimestamp?.isSameMonthAs(dateNow) != true
                                && after?.nextReminderTimestamp
                            ?.isSameMonthAs(dateNow) == true ->
                            ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.this_month))

                        before?.nextReminderTimestamp
                            ?.isAfter(
                                dateNow
                                    .plusMonths(1)
                                    .withDayOfMonth(1)
                                    .atStartOfDay()
                            ) != true
                                && after?.nextReminderTimestamp
                            ?.isAfter(
                                dateNow
                                    .plusMonths(1)
                                    .withDayOfMonth(1)
                                    .atStartOfDay()
                            ) == true ->
                            ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.upcoming))

                        before?.nextReminderTimestamp != null
                                && after != null
                                && after.nextReminderTimestamp == null ->
                            ScheduleListItemUiModel.TypeSeparator(UiText.StringResource(R.string.retired))

                        else -> null
                    }
                }
        }

    override suspend fun markScheduleAsPaid(
        id: Long
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                val schedule = repo.getScheduleById(id)
                    ?: throw ScheduleNotFoundThrowable()
                repo.createTransactionFromScheduleAndSetNextReminder(schedule)
                Resource.Success(Unit)
            }
        } catch (_: ScheduleNotFoundThrowable) {
            Resource.Error(UiText.StringResource(R.string.error_schedule_not_found))
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            Resource.Error(
                t.localizedMessage?.let {
                    UiText.DynamicString(it)
                } ?: UiText.StringResource(R.string.error_unknown)
            )
        }
    }

    override suspend fun deleteSchedulesById(ids: Set<Long>) = withContext(Dispatchers.IO) {
        repo.deleteSchedulesByIds(ids)
    }
}

class ScheduleNotFoundThrowable : Throwable("Schedule not found")