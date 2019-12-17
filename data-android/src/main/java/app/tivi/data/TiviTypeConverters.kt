/*
 * Copyright 2017 Google LLC
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

import androidx.room.TypeConverter
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Request
import app.tivi.data.entities.ShowStatus
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

object TiviTypeConverters {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    private val requestValues by lazy(LazyThreadSafetyMode.NONE) { Request.values() }
    private val imageTypeValues by lazy(LazyThreadSafetyMode.NONE) { ImageType.values() }
    private val pendingActionValues by lazy(LazyThreadSafetyMode.NONE) { PendingAction.values() }
    private val showStatusValues by lazy(LazyThreadSafetyMode.NONE) { ShowStatus.values() }
    private val dayOfWeekValues by lazy(LazyThreadSafetyMode.NONE) { DayOfWeek.values() }

    @TypeConverter
    @JvmStatic
    fun toOffsetDateTime(value: String?) = value?.let { formatter.parse(value, OffsetDateTime::from) }

    @TypeConverter
    @JvmStatic
    fun fromOffsetDateTime(date: OffsetDateTime?): String? = date?.format(formatter)

    @TypeConverter
    @JvmStatic
    fun toZoneId(value: String?) = value?.let { ZoneId.of(it) }

    @TypeConverter
    @JvmStatic
    fun fromZoneId(value: ZoneId?) = value?.id

    @TypeConverter
    @JvmStatic
    fun toLocalTime(value: String?) = value?.let { LocalTime.parse(value) }

    @TypeConverter
    @JvmStatic
    fun fromLocalTime(value: LocalTime?) = value?.format(DateTimeFormatter.ISO_LOCAL_TIME)

    @TypeConverter
    @JvmStatic
    fun toDayOfWeek(value: Int?): DayOfWeek? {
        return if (value != null) {
            dayOfWeekValues.firstOrNull { it.value == value }
        } else null
    }

    @TypeConverter
    @JvmStatic
    fun fromDayOfWeek(day: DayOfWeek?) = day?.value

    @TypeConverter
    @JvmStatic
    fun toInstant(value: Long?) = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    @JvmStatic
    fun fromInstant(date: Instant?) = date?.toEpochMilli()

    @TypeConverter
    @JvmStatic
    fun fromPendingAction(action: PendingAction): String = action.value

    @TypeConverter
    @JvmStatic
    fun toPendingAction(action: String?) = pendingActionValues.firstOrNull { it.value == action }

    @TypeConverter
    @JvmStatic
    fun fromRequest(value: Request) = value.tag

    @TypeConverter
    @JvmStatic
    fun toRequest(value: String) = requestValues.firstOrNull { it.tag == value }

    @TypeConverter
    @JvmStatic
    fun fromImageType(value: ImageType) = value.storageKey

    @TypeConverter
    @JvmStatic
    fun toImageType(value: String?) = imageTypeValues.firstOrNull { it.storageKey == value }

    @TypeConverter
    @JvmStatic
    fun fromShowStatus(value: ShowStatus?) = value?.storageKey

    @TypeConverter
    @JvmStatic
    fun toShowStatus(value: String?) = showStatusValues.firstOrNull { it.storageKey == value }
}
