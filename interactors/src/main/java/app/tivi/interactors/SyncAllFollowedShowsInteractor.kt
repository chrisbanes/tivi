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
import app.tivi.data.entities.PendingAction
import app.tivi.extensions.parallelForEach
import app.tivi.trakt.TraktAuthState
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Provider

class SyncAllFollowedShowsInteractor @Inject constructor(
    private val followedShowsDao: FollowedShowsDao,
    dispatchers: AppCoroutineDispatchers,
    private val loggedIn: Provider<TraktAuthState>,
    private val syncTraktFollowedShowsInteractor: SyncTraktFollowedShowsInteractor,
    private val syncFollowedShowInteractor: SyncFollowedShowInteractor
) : Interactor<SyncAllFollowedShowsInteractor.Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend operator fun invoke(param: Params) {
        val authed = loggedIn.get() == TraktAuthState.LOGGED_IN

        if (authed) {
            // First sync the followed shows to/from Trakt
            syncTraktFollowedShowsInteractor(SyncTraktFollowedShowsInteractor.Params(param.forceLoad))
        }

        // Now iterate through the followed shows and update them
        val followedShows = followedShowsDao.entries()
        followedShows.filter {
            it.pendingAction != PendingAction.DELETE
        }.parallelForEach(dispatcher) {
            syncFollowedShowInteractor(SyncFollowedShowInteractor.Params(it.showId, param.forceLoad))
        }
    }

    data class Params(val forceLoad: Boolean)
}