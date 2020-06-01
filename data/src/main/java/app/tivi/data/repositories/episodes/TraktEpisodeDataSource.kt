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

package app.tivi.data.repositories.episodes

import app.tivi.data.entities.Episode
import app.tivi.data.entities.ErrorResult
import app.tivi.data.entities.Result
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktEpisodeToEpisode
import app.tivi.extensions.executeWithRetry
import app.tivi.extensions.toResult
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Episodes
import javax.inject.Inject
import javax.inject.Provider

class TraktEpisodeDataSource @Inject constructor(
    private val traktIdMapper: ShowIdToTraktIdMapper,
    private val service: Provider<Episodes>,
    private val episodeMapper: TraktEpisodeToEpisode
) : EpisodeDataSource {
    override suspend fun getEpisode(
        showId: Long,
        seasonNumber: Int,
        episodeNumber: Int
    ): Result<Episode> {
        val traktId = traktIdMapper.map(showId)
            ?: return ErrorResult(IllegalArgumentException("No Trakt ID for show with ID: $showId"))

        return service.get().summary(traktId.toString(), seasonNumber, episodeNumber, Extended.FULL)
            .executeWithRetry()
            .toResult(episodeMapper::map)
    }
}
