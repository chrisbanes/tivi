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

package app.tivi.data.entities

interface TiviEntity {
    val id: Long
}

interface TraktIdEntity {
    val traktId: Int?
}

interface TmdbIdEntity {
    val tmdbId: Int?
}

interface TmdbImageEntity : TiviEntity {
    val path: String
    val type: ImageType
    val language: String?
    val rating: Float
    val isPrimary: Boolean
}

enum class ImageType(val storageKey: String) {
    BACKDROP("backdrop"),
    POSTER("poster"),
    LOGO("logo"),
}

internal fun <T : TmdbImageEntity> Collection<T>.findHighestRatedPoster(): T? {
    if (size <= 1) return firstOrNull()
    @Suppress("DEPRECATION") // Can't use maxByOrNull until we're API version 1.4
    return filter { it.type == ImageType.POSTER }
        .maxBy { it.rating + (if (it.isPrimary) 10f else 0f) }
}

internal fun <T : TmdbImageEntity> Collection<T>.findHighestRatedBackdrop(): T? {
    if (size <= 1) return firstOrNull()
    @Suppress("DEPRECATION") // Can't use maxByOrNull until we're API version 1.4
    return filter { it.type == ImageType.BACKDROP }
        .maxBy { it.rating + (if (it.isPrimary) 10f else 0f) }
}
