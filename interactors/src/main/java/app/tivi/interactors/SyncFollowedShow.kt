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

import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.trakt.TraktAuthState
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Provider

class SyncFollowedShow @Inject constructor(
    private val followedShowsDao: FollowedShowsDao,
    dispatchers: AppCoroutineDispatchers,
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val loggedIn: Provider<TraktAuthState>
) : Interactor<SyncFollowedShow.Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend operator fun invoke(param: Params) {
        val entry = followedShowsDao.entryWithShowId(param.showId)
                ?: throw IllegalArgumentException("Followed entry with showId: $param does not exist")

        val authed = loggedIn.get() == TraktAuthState.LOGGED_IN

        // Then update the seasons/episodes
        seasonsEpisodesRepository.updateSeasonsEpisodes(entry.showId)
        // Finally update any watched progress
        if (authed) {
            seasonsEpisodesRepository.syncEpisodeWatchesForShow(entry.showId)
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}