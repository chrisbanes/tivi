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

package me.banes.chris.tivi.trakt

import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.enums.Extended
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.data.daos.EntityInserter
import me.banes.chris.tivi.data.daos.EpisodesDao
import me.banes.chris.tivi.data.daos.SeasonsDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.Episode
import me.banes.chris.tivi.extensions.fetchBodyWithRetry
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktEpisodeFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val seasonDao: SeasonsDao,
    private val episodesDao: EpisodesDao,
    private val trakt: TraktV2,
    private val dispatchers: AppCoroutineDispatchers,
    private val entityInserter: EntityInserter
) {
    suspend fun loadShowSeasonEpisodes(seasonId: Long): List<Episode> {
        val episodes = withContext(dispatchers.database) { episodesDao.episodesFromSeasonId(seasonId) }
        if (episodes?.isNotEmpty() == true) return episodes

        val season = withContext(dispatchers.database) {
            seasonDao.seasonWithId(seasonId)
        } ?: throw IllegalArgumentException("Season with id[$seasonId] does not exist")
        val show = withContext(dispatchers.database) {
            showDao.getShowWithId(season.showId!!)!!
        }

        return withContext(dispatchers.network) {
            trakt.seasons().season(show.traktId.toString(), season.number!!, Extended.FULL).fetchBodyWithRetry()
        }.mapNotNull { traktEpisode ->
            withContext(dispatchers.database) {
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
                }.let {
                    entityInserter.insertOrUpdate(episodesDao, it)
                }.let {
                    episodesDao.episodeWithId(it)
                }
            }
        }
    }
}