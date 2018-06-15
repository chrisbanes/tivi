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

import app.tivi.SeasonFetcher
import app.tivi.datasources.RefreshableDataSource
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.entities.SeasonWithEpisodes
import app.tivi.util.AppRxSchedulers
import io.reactivex.Flowable
import javax.inject.Inject

class ShowSeasonsDataSource @Inject constructor(
    private val seasonsDao: SeasonsDao,
    private val schedulers: AppRxSchedulers,
    private val seasonFetcher: SeasonFetcher
) : RefreshableDataSource<Long, List<SeasonWithEpisodes>> {
    override suspend fun refresh(param: Long) {
        seasonFetcher.load(param)
    }

    override fun data(param: Long): Flowable<List<SeasonWithEpisodes>> {
        return seasonsDao.seasonsWithEpisodesForShowId(param)
                .subscribeOn(schedulers.io)
    }
}