/*
 * Copyright 2023 Google LLC
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

package app.tivi.data.db

import androidx.room.TypeConverter
import app.tivi.extensions.unsafeLazy
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toLocalTime

object DateTimeTypeConverters {
    private val dayOfWeekValues by unsafeLazy { DayOfWeek.values() }

    @TypeConverter
    @JvmStatic
    fun toDayOfWeek(value: Int?): DayOfWeek? = value?.let {
        dayOfWeekValues.firstOrNull { it.value == value }
    }

    @TypeConverter
    @JvmStatic
    fun fromDayOfWeek(day: DayOfWeek?) = day?.value

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.toLocalDateTime()

    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(date: LocalDateTime?): String? = date?.toString()

    @TypeConverter
    @JvmStatic
    fun toLocalTime(value: String?): LocalTime? = value?.toLocalTime()

    @TypeConverter
    @JvmStatic
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    @JvmStatic
    fun toZoneId(value: String?) = value?.let { TimeZone.of(it) }

    @TypeConverter
    @JvmStatic
    fun fromZoneId(value: TimeZone?) = value?.id

    @TypeConverter
    @JvmStatic
    fun toInstant(value: String?): Instant? = value?.toInstant()

    @TypeConverter
    @JvmStatic
    fun fromInstant(date: Instant?): String? = date?.toString()
}
