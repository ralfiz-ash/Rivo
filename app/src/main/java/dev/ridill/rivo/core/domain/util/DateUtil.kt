package dev.ridill.rivo.core.domain.util

import androidx.annotation.StringRes
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.util.UiText
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

object DateUtil {
    fun now(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime = LocalDateTime.now(zoneId)

    fun dateNow(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate = LocalDate.now(zoneId)

    fun parseDateTime(
        value: String,
        formatter: DateTimeFormatter = Formatters.isoLocalDateTime
    ): LocalDateTime = LocalDateTime.parse(value, formatter)

    fun parseDateTimeOrNull(
        value: String,
        formatter: DateTimeFormatter = Formatters.isoLocalDateTime
    ): LocalDateTime? = tryOrNull { LocalDateTime.parse(value, formatter) }

    fun getPartOfDay(): PartOfDay = when (now().hour) {
        in (0..11) -> PartOfDay.MORNING
        12 -> PartOfDay.NOON
        in (13..15) -> PartOfDay.AFTERNOON
        else -> PartOfDay.EVENING
    }

    fun fromMillis(
        millis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDateTime = Instant.ofEpochMilli(millis)
        .atZone(zoneId)
        .toLocalDateTime()

    fun toMillis(
        dateTime: LocalDateTime,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long = dateTime
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()

    fun toMillis(
        zonedDateTime: ZonedDateTime,
    ): Long = zonedDateTime
        .toInstant()
        .toEpochMilli()

    fun toMillis(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long = date
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()

    fun dateFromMillisWithTime(
        millis: Long,
        time: LocalDateTime = now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): LocalDateTime = Instant.ofEpochMilli(millis)
        .atZone(zoneId)
        .withHour(time.hour)
        .withMinute(time.minute)
        .withSecond(time.second)
        .withNano(time.nano)
        .toLocalDateTime()

    object Formatters {
        val isoLocalDateTime: DateTimeFormatter
            get() = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        val localizedDateMedium: DateTimeFormatter
            get() = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

        val localizedDateLong: DateTimeFormatter
            get() = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

        val localizedTimeShort: DateTimeFormatter
            get() = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

        val localizedDateMediumTimeShort: DateTimeFormatter
            get() = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

        val MMMM_yyyy_spaceSep: DateTimeFormatter
            get() = DateTimeFormatter.ofPattern("MMMM yyyy")

        val MMM_yy_spaceSep: DateTimeFormatter
            get() = DateTimeFormatter.ofPattern("MMM yy")

        val ddth_EEE_spaceSep: DateTimeFormatter
            get() = DateTimeFormatterBuilder()
                .appendText(ChronoField.DAY_OF_MONTH, ordinalsMap)
                .appendPattern(" EEE")
                .toFormatter()

        val MMM_ddth_spaceSep: DateTimeFormatter
            get() = DateTimeFormatterBuilder()
                .appendPattern("MMM ")
                .appendText(ChronoField.DAY_OF_MONTH, ordinalsMap)
                .toFormatter()

        fun prettyDateAgo(
            date: LocalDate
        ): UiText {
            val currentDate = now()
            if (date.isAfter(currentDate.toLocalDate())) return UiText.DynamicString(String.Empty)

            val daysDiff = ChronoUnit.DAYS.between(date, currentDate)
                .coerceAtLeast(Long.Zero)
                .toInt()

            if (daysDiff < 1)
                return UiText.StringResource(R.string.today)

            if (daysDiff <= 3)
                return UiText.PluralResource(R.plurals.days_past, daysDiff, daysDiff.toString())

            return UiText.DynamicString(date.format(localizedDateLong))
        }

        fun formatterWithDefault(
            pattern: String,
            dateTime: LocalDateTime = now()
        ): DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern(pattern)
            .parseDefaulting(ChronoField.YEAR, dateTime.year.toLong())
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, dateTime.monthValue.toLong())
            .parseDefaulting(ChronoField.DAY_OF_MONTH, dateTime.dayOfMonth.toLong())
            .parseDefaulting(ChronoField.HOUR_OF_DAY, dateTime.hour.toLong())
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, dateTime.minute.toLong())
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, dateTime.second.toLong())
            .toFormatter()

        fun formatterWithDefault(
            pattern: DateTimeFormatter,
            dateTime: LocalDateTime = now()
        ): DateTimeFormatter = DateTimeFormatterBuilder()
            .append(pattern)
            .parseDefaulting(ChronoField.YEAR, dateTime.year.toLong())
            .parseDefaulting(ChronoField.MONTH_OF_YEAR, dateTime.monthValue.toLong())
            .parseDefaulting(ChronoField.DAY_OF_MONTH, dateTime.dayOfMonth.toLong())
            .parseDefaulting(ChronoField.HOUR_OF_DAY, dateTime.hour.toLong())
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, dateTime.minute.toLong())
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, dateTime.second.toLong())
            .toFormatter()

        private val ordinalsMap: Map<Long, String>
            get() {
                val mutableMap = mutableMapOf(
                    1L to "1st",
                    2L to "2nd",
                    3L to "3rd",
                    21L to "21st",
                    22L to "22nd",
                    23L to "23rd",
                    31L to "31st",
                ).also { map ->
                    (1L..31L).forEach { map.putIfAbsent(it, "${it}th") }
                }

                return mutableMap.toMap()
            }
    }
}

fun LocalDateTime.isSameMonthAs(other: LocalDateTime?): Boolean =
    this.year == other?.year && this.month == other.month

fun LocalDateTime.isSameMonthAs(other: LocalDate?): Boolean =
    this.year == other?.year && this.month == other.month

enum class PartOfDay(
    @StringRes val labelRes: Int
) {
    MORNING(R.string.part_of_day_morning),
    NOON(R.string.part_of_day_noon),
    AFTERNOON(R.string.part_of_day_afternoon),
    EVENING(R.string.part_of_day_evening)
}