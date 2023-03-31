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

package app.tivi.data.util

import app.tivi.data.models.ImageType
import app.tivi.data.models.TmdbImageEntity

internal fun <T : TmdbImageEntity> findHighestRatedItem(items: Collection<T>, type: ImageType): T? {
    if (items.size <= 1) {
        return items.firstOrNull()
    }
    return items.asSequence()
        .filter { it.type == type }
        .maxByOrNull { it.rating + (if (it.isPrimary) 10f else 0f) }
}
