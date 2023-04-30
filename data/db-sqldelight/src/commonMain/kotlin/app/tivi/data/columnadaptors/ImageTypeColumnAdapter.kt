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

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.ImageType
import app.tivi.extensions.unsafeLazy

internal object ImageTypeColumnAdapter : ColumnAdapter<ImageType, String> {
    private val values by unsafeLazy { ImageType.values().associateBy(ImageType::storageKey) }

    override fun decode(databaseValue: String): ImageType = values.getValue(databaseValue)

    override fun encode(value: ImageType): String = value.storageKey
}
