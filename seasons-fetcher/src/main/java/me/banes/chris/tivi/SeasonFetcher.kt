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

package me.banes.chris.tivi

import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.data.DatabaseTransactionRunner
import me.banes.chris.tivi.data.daos.EntityInserter
import me.banes.chris.tivi.data.daos.SeasonsDao
import me.banes.chris.tivi.data.entities.Season
import me.banes.chris.tivi.inject.ApplicationLevel
import me.banes.chris.tivi.trakt.TraktSeasonFetcher
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import me.banes.chris.tivi.util.AppRxSchedulers
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeasonFetcher @Inject constructor(
    @ApplicationLevel private val disposables: CompositeDisposable,
    private val traktSeasonFetcher: TraktSeasonFetcher,
    private val transactionRunner: DatabaseTransactionRunner,
    private val entityInserter: EntityInserter,
    private val seasonsDao: SeasonsDao,
    private val schedulers: AppRxSchedulers,
    private val dispatchers: AppCoroutineDispatchers,
    private val episodeFetcher: EpisodeFetcher
) {
    fun load(showId: Long): Maybe<List<Season>> {
        return traktSeasonFetcher.loadShowSeasons(showId)
                .doOnSuccess {
                    it.forEach {
                        if (it.needsEpisodeUpdate()) {
                            updateEpisodesAsync(it)
                        }
                    }
                }
    }

    private fun updateEpisodesAsync(season: Season) {
        async {
            val episodes = episodeFetcher.load(season.id!!)
            if (episodes.isNotEmpty()) {
                withContext(dispatchers.database) {
                    transactionRunner.runInTransaction {
                        seasonsDao.seasonWithId(season.id!!).let {
                            it.lastEpisodeUpdate = OffsetDateTime.now()
                            entityInserter.insertOrUpdate(seasonsDao, it)
                        }
                    }
                }
            }
        }
    }
}