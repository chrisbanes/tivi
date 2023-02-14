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
import app.tivi.data.models.ImageType
import app.tivi.data.models.PendingAction
import app.tivi.data.models.Request
import app.tivi.data.models.ShowStatus
import app.tivi.data.models.SortOption
import app.tivi.extensions.unsafeLazy

object TiviTypeConverters {
    private val requestValues by unsafeLazy { Request.values() }
    private val imageTypeValues by unsafeLazy { ImageType.values() }
    private val pendingActionValues by unsafeLazy { PendingAction.values() }
    private val showStatusValues by unsafeLazy { ShowStatus.values() }

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

    @TypeConverter
    @JvmStatic
    fun fromSortOption(sortOption: SortOption): String = sortOption.sqlValue
}
