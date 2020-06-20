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

import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.resultentities.EpisodeWithSeasonWithShow
import app.tivi.domain.SubjectInteractor
import app.tivi.extensions.flatMapLatestNullable
import app.tivi.extensions.mapNullable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNextShowEpisodeToWatch @Inject constructor(
    private val followedShowsRepository: FollowedShowsRepository,
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository
) : SubjectInteractor<Unit, EpisodeWithSeasonWithShow?>() {

    override fun createObservable(params: Unit): Flow<EpisodeWithSeasonWithShow?> {
        return followedShowsRepository.observeNextShowToWatch().flatMapLatestNullable { nextShow ->
            seasonsEpisodesRepository.observeNextEpisodeToWatch(nextShow.entry.showId).mapNullable {
                EpisodeWithSeasonWithShow(it.episode!!, it.season!!, nextShow.show, nextShow.images)
            }
        }
    }
}
