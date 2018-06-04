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

package app.tivi.datasources.trakt

import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.datasources.RefreshableDataSource
import app.tivi.util.AppRxSchedulers
import io.reactivex.Flowable
import javax.inject.Inject

class EpisodeWatchesDataSource @Inject constructor(
    private val dao: EpisodeWatchEntryDao,
    private val schedulers: AppRxSchedulers
) : RefreshableDataSource<Long, List<EpisodeWatchEntry>> {
    override suspend fun refresh(param: Long) {
        // TODO
    }

    override fun data(param: Long): Flowable<List<EpisodeWatchEntry>> {
        return dao.watchesForEpisodeFlowable(param)
                .subscribeOn(schedulers.database)
                .distinctUntilChanged()
    }
}
