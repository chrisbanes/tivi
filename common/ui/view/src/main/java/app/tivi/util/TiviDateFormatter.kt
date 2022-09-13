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
import app.tivi.inject.MediumDate
import app.tivi.inject.MediumDateTime
import app.tivi.inject.ShortDate
import app.tivi.inject.ShortTime
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.Temporal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TiviDateFormatter @Inject constructor(
    @ShortTime private val shortTimeFormatter: DateTimeFormatter,
    @ShortDate private val shortDateFormatter: DateTimeFormatter,
    @MediumDate private val mediumDateFormatter: DateTimeFormatter,
    @MediumDateTime private val mediumDateTimeFormatter: DateTimeFormatter
) {
    fun formatShortDate(temporalAmount: Temporal): String = shortDateFormatter.format(temporalAmount)

    fun formatMediumDate(temporalAmount: Temporal): String = mediumDateFormatter.format(temporalAmount)

    fun formatMediumDateTime(temporalAmount: Temporal): String = mediumDateTimeFormatter.format(temporalAmount)

    fun formatShortTime(localTime: LocalTime): String = shortTimeFormatter.format(localTime)

    fun formatShortRelativeTime(dateTime: OffsetDateTime): String {
        val now = OffsetDateTime.now()

        return if (dateTime.isBefore(now)) {
            if (dateTime.year == now.year || dateTime.isAfter(now.minusDays(7))) {
                // Within the past week
                DateUtils.getRelativeTimeSpanString(
                    dateTime.toInstant().toEpochMilli(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_DATE
                ).toString()
            } else {
                // More than 7 days ago
                formatShortDate(dateTime)
            }
        } else {
            if (dateTime.year == now.year || dateTime.isBefore(now.plusDays(14))) {
                // In the near future (next 2 weeks)
                DateUtils.getRelativeTimeSpanString(
                    dateTime.toInstant().toEpochMilli(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_DATE
                ).toString()
            } else {
                // In the far future
                formatShortDate(dateTime)
            }
        }
    }
}
