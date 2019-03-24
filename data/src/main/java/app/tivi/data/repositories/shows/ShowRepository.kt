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

package app.tivi.data.repositories.shows

import app.tivi.data.entities.TiviShow
import io.reactivex.Flowable

interface ShowRepository {
    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    suspend fun getShow(showId: Long): TiviShow

    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    suspend fun updateShow(showId: Long)

    suspend fun needsUpdate(showId: Long): Boolean

    fun observeShow(showId: Long): Flowable<TiviShow>
}