// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import kotlin.time.Duration.Companion.days
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toNSDate
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendar.Companion.currentCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateComponentsFormatter
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSRelativeDateTimeFormatter
import platform.Foundation.NSRelativeDateTimeFormatterStyleNamed
import platform.Foundation.autoupdatingCurrentLocale

actual class TiviDateFormatter(
    locale: NSLocale = NSLocale.autoupdatingCurrentLocale,
) {
    private val shortDate by lazy {
        NSDateFormatter().apply {
            this.locale = locale
            dateStyle = NSDateFormatterShortStyle
        }
    }
    private val shortTime by lazy {
        NSDateComponentsFormatter().apply {
            calendar = NSCalendar.currentCalendar.apply {
                this.locale = locale
            }
        }
    }
    private val mediumDate by lazy {
        NSDateFormatter().apply {
            this.locale = locale
            dateStyle = NSDateFormatterMediumStyle
        }
    }
    private val mediumDateTime by lazy {
        NSDateFormatter().apply {
            this.locale = locale
            dateStyle = NSDateFormatterMediumStyle
            timeStyle = NSDateFormatterMediumStyle
        }
    }
    private val interval by lazy {
        NSRelativeDateTimeFormatter().apply {
            dateTimeStyle = NSRelativeDateTimeFormatterStyleNamed
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

    private fun LocalDate.toNSDate(calendar: NSCalendar = currentCalendar): NSDate {
        val components = toNSDateComponents()
        components.calendar = calendar
        return components.date ?: error("Error while formatting LocalDate: $this")
    }

    private fun LocalTime.toNSDateComponents(): NSDateComponents {
        val components = NSDateComponents()
        components.hour = hour.convert()
        components.minute = minute.convert()
        components.second = second.convert()
        components.nanosecond = nanosecond.convert()
        return components
    }
}
