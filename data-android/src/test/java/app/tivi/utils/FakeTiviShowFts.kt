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

package app.tivi.utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A fake version of TiviShowFts to model the same entity
 * table, but without actually using FTS. This is because
 * Robolectric's SQL implementation does not support everything
 * needed for Room's FTS support.
 */
@Entity(tableName = "shows_fts")
internal data class FakeTiviShowFts(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long? = null,
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "original_title") val originalTitle: String? = null,
    @ColumnInfo(name = "docid") val docId: Long? = null
)
