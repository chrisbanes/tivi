package com.vanpra.composematerialdialogs.datetime.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlin.math.cos
import kotlin.math.sin

internal fun Float.getOffset(angle: Double): Offset =
    Offset((this * cos(angle)).toFloat(), (this * sin(angle)).toFloat())

/*internal val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(this.year, this.month)*/

internal expect val LocalDate.isLeapYear: Boolean

internal val LocalTime.isAM: Boolean
    get() = this.hour in 0..11

internal val LocalTime.simpleHour: Int
    get() {
        val tempHour = this.hour % 12
        return if (tempHour == 0) 12 else tempHour
    }

internal expect fun Month.getShortLocalName(locale: Locale): String

internal expect fun Month.getFullLocalName(locale: Locale): String

internal expect fun DayOfWeek.getShortLocalName(locale: Locale): String

internal expect fun Month.testLength(year: Int, isLeapYear: Boolean): Int

internal fun LocalTime.toAM(): LocalTime = if (this.isAM) this else this.minusHours(12)
internal fun LocalTime.toPM(): LocalTime = if (!this.isAM) this else this.plusHours(12)

internal expect fun LocalTime.minusHours(hoursToSubtract: Long): LocalTime
internal expect fun LocalTime.plusHours(hoursToAdd: Long): LocalTime

internal fun LocalTime.noSeconds(): LocalTime = LocalTime(this.hour, this.minute, 0, 0)

internal fun LocalTime.withHour(hour: Int): LocalTime = LocalTime(hour, this.minute, this.second, this.nanosecond)

internal fun LocalTime.withMinute(minute: Int): LocalTime = LocalTime(this.hour, minute, this.second, this.nanosecond)

internal fun LocalTime.withSecond(second: Int): LocalTime = LocalTime(this.hour, this.minute, second, this.nanosecond)

internal fun LocalTime.withNanosecond(nanosecond: Int): LocalTime = LocalTime(this.hour, this.minute, this.second, nanosecond)

internal fun LocalDate.withDayOfMonth(dayOfMonth: Int) = LocalDate(this.year, this.month, dayOfMonth)

private val minTime = LocalTime(0, 0, 0, 0)
internal val LocalTime.Companion.Min: LocalTime get() = minTime

private val maxTime = LocalTime(23, 59, 59, 999_999_999)
internal val LocalTime.Companion.Max: LocalTime get() = maxTime

internal expect fun DayOfWeek.plusDays(days: Long): DayOfWeek

internal expect fun DayOfWeek.getNarrowDisplayName(locale: Locale): String
