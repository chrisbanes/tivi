// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.inject.ActivityScope
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.Temporal
import java.util.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import me.tatarka.inject.annotations.Inject

@ActivityScope
@Inject
actual class TiviDateFormatter(
    private val locale: Locale = Locale.getDefault(),
) {
    private val shortDate: DateTimeFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(locale)
    }
    private val shortTime: DateTimeFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale)
    }
    private val mediumDate: DateTimeFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
    }
    private val mediumDateTime: DateTimeFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(locale)
    }

    private fun Instant.toTemporal(): Temporal {
        return JavaLocalDateTime.ofInstant(toJavaInstant(), ZoneId.systemDefault())
    }

    actual fun formatShortDate(instant: Instant): String {
        return shortDate.format(instant.toTemporal())
    }

    actual fun formatShortDate(date: LocalDate): String {
        return shortDate.format(date.toJavaLocalDate())
    }

    actual fun formatMediumDate(instant: Instant): String {
        return mediumDate.format(instant.toTemporal())
    }

    actual fun formatMediumDateTime(instant: Instant): String {
        return mediumDateTime.format(instant.toTemporal())
    }

    actual fun formatShortTime(localTime: LocalTime): String {
        return shortTime.format(localTime.toJavaLocalTime())
    }

    actual fun formatShortRelativeTime(date: Instant, reference: Instant): String {
        // TODO: Look at using RelativeDateTimeFormatter
        return formatShortDate(date)
    }
}
