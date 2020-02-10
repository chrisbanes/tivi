/*
 * Copyright 2019 Google LLC
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

package app.tivi.data

import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.Period
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAmount

fun TemporalAmount.inPast(): Instant = Instant.now().minus(this)

fun periodOf(years: Int = 0, months: Int = 0, days: Int = 0) = Period.of(years, months, days)

fun instantInPast(days: Int = 0, hours: Int = 0, minutes: Int = 0): Instant {
    var instant = Instant.now()
    if (days != 0) {
        instant = instant.minus(days.toLong(), ChronoUnit.DAYS)
    }
    if (hours != 0) {
        instant = instant.minus(hours.toLong(), ChronoUnit.HOURS)
    }
    if (minutes != 0) {
        instant = instant.minus(minutes.toLong(), ChronoUnit.HOURS)
    }
    return instant
}

fun OffsetDateTime.isBefore(instant: Instant): Boolean = toInstant().isBefore(instant)
