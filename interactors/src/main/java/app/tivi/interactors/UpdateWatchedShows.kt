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

import android.arch.paging.DataSource
import app.tivi.data.repositories.watchedshows.WatchedShowsRepository
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject

class UpdateWatchedShows @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val watchedShowsRepository: WatchedShowsRepository
) : PagingInteractor<UpdateWatchedShows.Params, WatchedShowEntryWithShow> {
    override val dispatcher: CoroutineDispatcher
        get() = dispatchers.io

    override suspend fun invoke(param: Params) {
        watchedShowsRepository.updateWatchedShows()
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, WatchedShowEntryWithShow> {
        return watchedShowsRepository.observeWatchedShowsPagedList()
    }

    data class Params(val forceLoad: Boolean)
}
