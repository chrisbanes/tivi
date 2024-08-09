// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import kotlin.time.Duration.Companion.days
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toNSDate
import kotlinx.datetime.toNSDateComponents
import kotlinx.datetime.toNSTimeZone
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendar.Companion.currentCalendar
import platform.Foundation.NSCalendarMatchNextTime
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateComponentsFormatter
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSRelativeDateTimeFormatter
import platform.Foundation.NSRelativeDateTimeFormatterStyleNamed

@OptIn(ExperimentalForeignApi::class)
@Inject
actual class TiviDateFormatter(
  private val overrideLocale: NSLocale? = null,
  internal val overrideTimeZone: TimeZone? = null,
) {
  internal val calendar: NSCalendar by lazy {
    NSCalendar.currentCalendar.apply {
      if (overrideLocale != null) {
        locale = overrideLocale
      }
      if (overrideTimeZone != null) {
        timeZone = overrideTimeZone.toNSTimeZone()
      }
    }
  }

  private val shortDate by lazy {
    createDateFormatter().apply {
      dateStyle = NSDateFormatterShortStyle
    }
  }
  private val shortTime by lazy {
    NSDateComponentsFormatter().apply {
      setAllowedUnits(NSCalendarUnitHour or NSCalendarUnitMinute)
      calendar = this@TiviDateFormatter.calendar
    }
  }
  private val mediumDate by lazy {
    createDateFormatter().apply {
      dateStyle = NSDateFormatterMediumStyle
    }
  }
  private val mediumDateTime by lazy {
    createDateFormatter().apply {
      dateStyle = NSDateFormatterMediumStyle
      timeStyle = NSDateFormatterMediumStyle
    }
  }
  private val interval by lazy {
    NSRelativeDateTimeFormatter().apply {
      calendar = this@TiviDateFormatter.calendar
      if (overrideLocale != null) {
        locale = overrideLocale
      }
      dateTimeStyle = NSRelativeDateTimeFormatterStyleNamed
    }
  }
  private val dayOfWeekFormatter by lazy {
    createDateFormatter().apply {
      setDateFormat("EEEE")
    }
  }

  actual fun formatShortDate(instant: Instant): String {
    return shortDate.stringFromDate(instant.toNSDate())
  }

  actual fun formatShortDate(date: LocalDate): String {
    return shortDate.stringFromDate(date.toNSDate())
  }

  actual fun formatMediumDate(instant: Instant): String {
    return mediumDate.stringFromDate(instant.toNSDate())
  }

  actual fun formatMediumDateTime(instant: Instant): String {
    return mediumDateTime.stringFromDate(instant.toNSDate())
  }

  actual fun formatShortTime(localTime: LocalTime): String {
    return shortTime.stringFromDateComponents(localTime.toNSDateComponents())
      ?: error("Error while formatting LocalTime: $localTime")
  }

  actual fun formatShortRelativeTime(date: Instant, reference: Instant): String = when {
    // Within the past week
    date < reference && (reference - date) < 7.days -> {
      interval.localizedStringForDate(date.toNSDate(), reference.toNSDate())
    }
    // In the near future (next 2 weeks)
    date > reference && (date - reference) < 14.days -> {
      interval.localizedStringForDate(date.toNSDate(), reference.toNSDate())
    }
    // In the far past/future
    else -> formatShortDate(date)
  }

  actual fun formatDayOfWeek(dayOfWeek: DayOfWeek): String {
    val date = NSDateComponents()
      .apply { weekday = dayOfWeek.toNSWeekdayUnit().convert() }
      .let { component ->
        calendar.nextDateAfterDate(
          date = NSDate(),
          matchingComponents = component,
          options = NSCalendarMatchNextTime,
        )
      }

    return date?.let(dayOfWeekFormatter::stringFromDate).orEmpty()
  }

  private fun LocalDate.toNSDate(calendar: NSCalendar = currentCalendar): NSDate {
    val components = toNSDateComponents()
    components.calendar = calendar
    return components.date ?: error("Error while formatting LocalDate: $this")
  }

  private fun createDateFormatter(): NSDateFormatter = NSDateFormatter().apply {
    calendar = this@TiviDateFormatter.calendar
    if (overrideLocale != null) {
      locale = overrideLocale
    }
    if (overrideTimeZone != null) {
      timeZone = overrideTimeZone.toNSTimeZone()
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun LocalTime.toNSDateComponents(): NSDateComponents {
  val components = NSDateComponents()
  components.hour = hour.convert()
  components.minute = minute.convert()
  components.second = second.convert()
  components.nanosecond = nanosecond.convert()
  return components
}
