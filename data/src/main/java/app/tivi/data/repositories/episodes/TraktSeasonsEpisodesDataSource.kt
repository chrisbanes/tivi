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

package app.tivi.data.repositories.episodes

import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.Season
import app.tivi.data.mappers.EpisodeIdToTraktIdMapper
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktEpisodeToEpisode
import app.tivi.data.mappers.TraktHistoryItemToEpisodeWatchEntry
import app.tivi.data.mappers.TraktSeasonToSeason
import app.tivi.extensions.fetchBody
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.trakt5.entities.EpisodeIds
import com.uwetrottmann.trakt5.entities.SyncEpisode
import com.uwetrottmann.trakt5.entities.SyncItems
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.HistoryType
import com.uwetrottmann.trakt5.services.Seasons
import com.uwetrottmann.trakt5.services.Sync
import com.uwetrottmann.trakt5.services.Users
import javax.inject.Inject
import javax.inject.Provider

class TraktSeasonsEpisodesDataSource @Inject constructor(
    private val showIdToTraktIdMapper: ShowIdToTraktIdMapper,
    private val episodeIdToTraktIdMapper: EpisodeIdToTraktIdMapper,
    private val seasonsService: Provider<Seasons>,
    private val usersService: Provider<Users>,
    private val syncService: Provider<Sync>,
    private val seasonMapper: TraktSeasonToSeason,
    private val episodeMapper: TraktEpisodeToEpisode,
    private val historyItemMapper: TraktHistoryItemToEpisodeWatchEntry
) : SeasonsEpisodesDataSource {
    override suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>> {
        val showTraktId = showIdToTraktIdMapper.map(showId) ?: return emptyList()

        return seasonsService.get().summary(showTraktId.toString(), Extended.FULLEPISODES)
                .fetchBodyWithRetry()
                .map {
                    seasonMapper.map(it).copy(showId = showId) to it.episodes.map(episodeMapper::map)
                }
    }

    override suspend fun getShowEpisodeWatches(showId: Long): List<Pair<Episode, EpisodeWatchEntry>> {
        val showTraktId = showIdToTraktIdMapper.map(showId) ?: return emptyList()

        return usersService.get().history(UserSlug.ME, HistoryType.SHOWS, showTraktId,
                0, 10000, Extended.NOSEASONS, null, null)
                .fetchBodyWithRetry()
                .filter { it.type == "episode" }
                .map { episodeMapper.map(it.episode) to historyItemMapper.map(it) }
    }

    override suspend fun getEpisodeWatches(episodeId: Long): List<EpisodeWatchEntry> {
        val episodeTraktId = episodeIdToTraktIdMapper.map(episodeId) ?: return emptyList()

        return usersService.get().history(UserSlug.ME, HistoryType.EPISODES, episodeTraktId,
                0, 10000, Extended.NOSEASONS, null, null)
                .fetchBodyWithRetry()
                .map { historyItemMapper.map(it) }
    }

    override suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        if (watches.isNotEmpty()) {
            val items = SyncItems()
            items.episodes = watches.mapNotNull {
                episodeIdToTraktIdMapper.map(it.episodeId)?.let {
                    SyncEpisode().id(EpisodeIds.trakt(it))
                }
            }

            val response = syncService.get().addItemsToWatchedHistory(items).fetchBody()
            // TODO check response
        }
    }

    override suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        val traktIds = watches.mapNotNull { it.traktId }
        if (traktIds.isNotEmpty()) {
            val items = SyncItems()
            items.ids = traktIds

            val response = syncService.get().deleteItemsFromWatchedHistory(items).fetchBody()
            // TODO check response
        }
    }
}