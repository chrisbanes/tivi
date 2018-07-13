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
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject

class FollowShowInteractor @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val followedShowsDao: FollowedShowsDao,
    private val syncFollowedShowInteractor: SyncFollowedShowInteractor,
    private val syncTraktFollowedShowsInteractor: SyncTraktFollowedShowsInteractor
) : Interactor<FollowShowInteractor.Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend operator fun invoke(param: Params) {
        followedShowsDao.insert(FollowedShowEntry(showId = param.showId, pendingAction = PendingAction.UPLOAD))
        // Now refresh the show
        syncFollowedShowInteractor(SyncFollowedShowInteractor.Params(param.showId, param.forceLoad))
        // Now sync followed shows
        syncTraktFollowedShowsInteractor(SyncTraktFollowedShowsInteractor.Params(param.forceLoad))
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}