/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.util

import android.text.format.DateUtils
import app.tivi.inject.ActivityScope
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.Temporal
import java.util.Locale
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@ActivityScope
@Inject
class TiviDateFormatter(
    private val locale: Locale,
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
        return LocalDateTime.ofInstant(toJavaInstant(), ZoneId.systemDefault())
    }

    fun formatShortDate(instant: Instant): String {
        return shortDate.format(instant.toTemporal())
    }

    fun formatMediumDate(instant: Instant): String {
        return mediumDate.format(instant.toTemporal())
    }

    fun formatMediumDateTime(instant: Instant): String {
        return mediumDateTime.format(instant.toTemporal())
    }

    fun formatShortTime(localTime: LocalTime): String {
        return shortTime.format(localTime)
    }

    fun formatShortRelativeTime(dateTime: Instant): String {
        val nowInstant = kotlinx.datetime.Clock.System.now()
        val now = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        val localDateTime = dateTime.toLocalDateTime(TimeZone.currentSystemDefault())

        return if (dateTime < nowInstant) {
            if (localDateTime.year == now.year || dateTime > (nowInstant - 7.days)) {
                // Within the past week
                DateUtils.getRelativeTimeSpanString(
                    dateTime.toEpochMilliseconds(),
                    nowInstant.toEpochMilliseconds(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_DATE,
                ).toString()
            } else {
                // More than 7 days ago
                formatShortDate(dateTime)
            }
        } else {
            if (localDateTime.year == now.year || dateTime > (nowInstant - 14.days)) {
                // In the near future (next 2 weeks)
                DateUtils.getRelativeTimeSpanString(
                    dateTime.toEpochMilliseconds(),
                    nowInstant.toEpochMilliseconds(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_DATE,
                ).toString()
            } else {
                // In the far future
                formatShortDate(dateTime)
            }
        }
    }
}
