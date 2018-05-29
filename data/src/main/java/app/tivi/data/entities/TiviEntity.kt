/*
 * Copyright 2018 Google, Inc.
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

import org.threeten.bp.OffsetDateTime
import kotlin.reflect.KMutableProperty0

interface TiviEntity {
    val id: Long?

    fun <T> updateProperty(entityVar: KMutableProperty0<T?>, updateVal: T?) {
        when {
            updateVal != null -> entityVar.set(updateVal)
        }
    }
}

interface TraktIdEntity {
    val traktId: Int?
    val lastTraktUpdate: OffsetDateTime?

    fun needsUpdateFromTrakt(): Boolean {
        return traktId != null && (lastTraktUpdate?.isBefore(OffsetDateTime.now().minusDays(1)) != false)
    }
}

interface TmdbIdEntity {
    val tmdbId: Int?
    val lastTmdbUpdate: OffsetDateTime?

    fun needsUpdateFromTmdb(): Boolean {
        return tmdbId != null && (lastTmdbUpdate?.isBefore(OffsetDateTime.now().minusDays(1)) != false)
    }
}