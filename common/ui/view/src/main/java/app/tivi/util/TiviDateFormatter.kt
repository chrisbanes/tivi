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
import app.tivi.datetime.DateTimeFormatters
import me.tatarka.inject.annotations.Inject
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.Temporal

@Inject
class TiviDateFormatter(
    private val formatters: DateTimeFormatters,
) {
    fun formatShortDate(temporalAmount: Temporal): String {
        return formatters.shortDate.format(temporalAmount)
    }

    fun formatMediumDate(temporalAmount: Temporal): String {
        return formatters.mediumDate.format(temporalAmount)
    }

    fun formatMediumDateTime(temporalAmount: Temporal): String {
        return formatters.mediumDateTime.format(temporalAmount)
    }

    fun formatShortTime(localTime: LocalTime): String {
        return formatters.shortTime.format(localTime)
    }

    fun formatShortRelativeTime(dateTime: OffsetDateTime): String {
        val now = OffsetDateTime.now()

        return if (dateTime.isBefore(now)) {
            if (dateTime.year == now.year || dateTime.isAfter(now.minusDays(7))) {
                // Within the past week
                DateUtils.getRelativeTimeSpanString(
                    dateTime.toEpochSecond() * 1000,
                    now.toEpochSecond() * 1000,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_DATE,
                ).toString()
            } else {
                // More than 7 days ago
                formatShortDate(dateTime)
            }
        } else {
            if (dateTime.year == now.year || dateTime.isBefore(now.plusDays(14))) {
                // In the near future (next 2 weeks)
                DateUtils.getRelativeTimeSpanString(
                    dateTime.toEpochSecond() * 1000,
                    now.toEpochSecond() * 1000,
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
