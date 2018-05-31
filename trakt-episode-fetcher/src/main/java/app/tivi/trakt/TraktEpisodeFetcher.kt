/*
 * Copyright 2017 Google, Inc.
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

package app.tivi.trakt

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.Episode
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.util.AppCoroutineDispatchers
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Episodes
import kotlinx.coroutines.experimental.withContext
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import com.uwetrottmann.trakt5.entities.Episode as TraktEpisode

@Singleton
class TraktEpisodeFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val seasonsDao: SeasonsDao,
    private val episodesDao: EpisodesDao,
    private val episodesService: Provider<Episodes>,
    private val dispatchers: AppCoroutineDispatchers,
    private val entityInserter: EntityInserter,
    private val transactionRunner: DatabaseTransactionRunner
) {
    suspend fun updateEpisodeData(episodeId: Long) {
        val episode = withContext(dispatchers.database) {
            episodesDao.episodeWithId(episodeId)
        } ?: throw IllegalArgumentException("Episode with id[$episodeId] does not exist")

        val season = withContext(dispatchers.database) {
            seasonsDao.seasonWithId(episode.seasonId!!)
        } ?: throw IllegalArgumentException("Season with id[${episode.seasonId}] does not exist")

        val show = withContext(dispatchers.database) {
            showDao.getShowWithId(season.showId!!)
        } ?: throw IllegalArgumentException("Show with id[${season.showId}] does not exist")

        val response = withContext(dispatchers.network) {
            episodesService.get()
                    .summary(show.traktId.toString(), season.number!!, episode.number!!, Extended.FULL)
                    .fetchBodyWithRetry()
        }

        withContext(dispatchers.database) {
            transactionRunner.runInTransaction {
                upsertEpisode(season.id!!, response)
            }
        }
    }

    fun upsertEpisode(seasonId: Long, traktEpisode: TraktEpisode) {
        (episodesDao.episodeWithTraktId(traktEpisode.ids.trakt) ?: Episode()).apply {
            updateProperty(this::seasonId, seasonId)
            updateProperty(this::traktId, traktEpisode.ids.trakt)
            updateProperty(this::tmdbId, traktEpisode.ids.tmdb)
            updateProperty(this::title, traktEpisode.title)
            updateProperty(this::number, traktEpisode.number)
            updateProperty(this::summary, traktEpisode.overview)
            updateProperty(this::firstAired, traktEpisode.first_aired)
            updateProperty(this::rating, traktEpisode.rating?.toFloat())
            updateProperty(this::votes, traktEpisode.votes)
            lastTraktUpdate = OffsetDateTime.now()
        }.also {
            entityInserter.insertOrUpdate(episodesDao, it)
        }
    }
}