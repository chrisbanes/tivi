// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import java.time.LocalDateTime as JavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.Temporal
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
actual class TiviDateFormatter(
  private val locale: Locale = Locale.getDefault(),
  internal val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
  private val shortDateFormatter: DateTimeFormatter by lazy {
    DateTimeFormatter
      .ofLocalizedDate(FormatStyle.SHORT)
      .withLocale(locale)
      .withZone(timeZone.toJavaZoneId())
  }
  private val shortTimeFormatter: DateTimeFormatter by lazy {
    DateTimeFormatter
      .ofLocalizedTime(FormatStyle.SHORT)
      .withLocale(locale)
      .withZone(timeZone.toJavaZoneId())
  }
  private val mediumDateFormatter: DateTimeFormatter by lazy {
    DateTimeFormatter
      .ofLocalizedDate(FormatStyle.MEDIUM)
      .withLocale(locale)
      .withZone(timeZone.toJavaZoneId())
  }
  private val mediumDateTimeFormatter: DateTimeFormatter by lazy {
    DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.MEDIUM)
      .withLocale(locale)
      .withZone(timeZone.toJavaZoneId())
  }
  private val dayOfWeekFormatter: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("EEEE")
      .withLocale(locale)
      .withZone(timeZone.toJavaZoneId())
  }

  private fun Instant.toTemporal(): Temporal {
    return JavaLocalDateTime.ofInstant(toJavaInstant(), timeZone.toJavaZoneId())
  }

  actual fun formatShortDate(instant: Instant): String {
    return shortDateFormatter.format(instant.toTemporal())
  }

  actual fun formatShortDate(date: LocalDate): String {
    return shortDateFormatter.format(date.toJavaLocalDate())
  }

  actual fun formatMediumDate(instant: Instant): String {
    return mediumDateFormatter.format(instant.toTemporal())
  }

  actual fun formatMediumDateTime(instant: Instant): String {
    return mediumDateTimeFormatter.format(instant.toTemporal())
  }

  actual fun formatShortTime(localTime: LocalTime): String {
    return shortTimeFormatter.format(localTime.toJavaLocalTime())
  }

  actual fun formatShortRelativeTime(date: Instant, reference: Instant): String {
    // TODO: Look at using RelativeDateTimeFormatter
    return formatShortDate(date)
  }

  actual fun formatDayOfWeek(dayOfWeek: DayOfWeek): String {
    return Clock.System.now()
      .toLocalDateTime(timeZone)
      .toJavaLocalDateTime()
      .with(TemporalAdjusters.nextOrSame(dayOfWeek.toJavaDayOfWeek()))
      .let { dayOfWeekFormatter.format(it) }
  }
}

private fun DayOfWeek.toJavaDayOfWeek(): java.time.DayOfWeek = when (this) {
  java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
  java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
  java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
  java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
  java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
  java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
  java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
}
