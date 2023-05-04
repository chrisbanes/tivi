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

package app.tivi.data.daos

import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.Season
import kotlinx.coroutines.flow.Flow

interface SeasonsDao : EntityDao<Season> {

    fun seasonsWithEpisodesForShowId(showId: Long): Flow<List<SeasonWithEpisodesAndWatches>>

    fun observeSeasonWithId(id: Long): Flow<Season>

    fun seasonsForShowId(showId: Long): List<Season>

    fun deleteWithShowId(showId: Long)

    fun seasonWithId(id: Long): Season?

    fun traktIdForId(id: Long): Int?

    fun seasonWithTraktId(traktId: Int): Season?

    fun showPreviousSeasonIds(seasonId: Long): LongArray

    fun updateSeasonIgnoreFlag(seasonId: Long, ignored: Boolean)

    fun seasonWithShowIdAndNumber(showId: Long, number: Int): Season?
}
