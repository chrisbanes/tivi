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

package app.tivi.interactors

import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.extensions.emptyFlowableList
import app.tivi.util.AppCoroutineDispatchers
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject

class UpdateFollowedShowSeasonData @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val followedShowsRepository: FollowedShowsRepository
) : SubjectInteractor<UpdateFollowedShowSeasonData.Params, List<SeasonWithEpisodesAndWatches>>() {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend fun execute(param: Params) {
        if (followedShowsRepository.isShowFollowed(param.showId)) {
            // Then update the seasons/episodes
            if (param.forceLoad || seasonsEpisodesRepository.needShowSeasonsUpdate(param.showId)) {
                seasonsEpisodesRepository.updateSeasonsEpisodes(param.showId)
            }
            // Finally update any watched progress
            if (param.forceLoad || seasonsEpisodesRepository.needShowEpisodeWatchesSync(param.showId)) {
                seasonsEpisodesRepository.syncEpisodeWatchesForShow(param.showId)
            }
        }
    }

    override fun createObservable(param: Params): Flowable<List<SeasonWithEpisodesAndWatches>> {
        return seasonsEpisodesRepository.observeSeasonsForShow(param.showId)
                .startWith(emptyFlowableList())
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}