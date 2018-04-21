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

package me.banes.chris.tivi.trakt.calls

import io.reactivex.Flowable
import kotlinx.coroutines.experimental.rx2.await
import me.banes.chris.tivi.SeasonFetcher
import me.banes.chris.tivi.calls.Call
import me.banes.chris.tivi.data.daos.SeasonsDao
import me.banes.chris.tivi.data.entities.SeasonWithEpisodes
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

class ShowSeasonsCall @Inject constructor(
    private val seasonsDao: SeasonsDao,
    private val schedulers: AppRxSchedulers,
    private val seasonFetcher: SeasonFetcher
) : Call<Long, List<SeasonWithEpisodes>> {
    override suspend fun refresh(param: Long) {
        seasonFetcher.load(param).await()
    }

    override fun data(param: Long): Flowable<List<SeasonWithEpisodes>> {
        return seasonsDao.seasonsWithEpisodesForShowId(param)
                .subscribeOn(schedulers.database)
    }
}