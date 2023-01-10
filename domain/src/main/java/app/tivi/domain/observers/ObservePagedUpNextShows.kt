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

package app.tivi.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.SortOption
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.resultentities.EpisodeWithSeasonWithShow
import app.tivi.domain.PagingInteractor
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ObservePagedUpNextShows @Inject constructor(
    private val followedShowsDao: FollowedShowsDao,
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
) : PagingInteractor<ObservePagedUpNextShows.Parameters, EpisodeWithSeasonWithShow>() {

    override fun createObservable(
        params: Parameters,
    ): Flow<PagingData<EpisodeWithSeasonWithShow>> {
        return Pager(config = params.pagingConfig) { followedShowsDao.pagedToWatchShows() }
            .flow
            .map { data ->
                data.map { entry ->
                    seasonsEpisodesRepository
                        .observeNextEpisodeToWatch(entry.show.id)
                        .filterNotNull()
                        .first()
                        .let { seasonEpisode ->
                            EpisodeWithSeasonWithShow(
                                episode = seasonEpisode.episode!!,
                                season = seasonEpisode.season!!,
                                show = entry.show,
                                images = entry.images
                            )
                        }
                }
            }
    }

    data class Parameters(
        val filter: String? = null,
        val sort: SortOption,
        override val pagingConfig: PagingConfig,
    ) : PagingInteractor.Parameters<EpisodeWithSeasonWithShow>
}
