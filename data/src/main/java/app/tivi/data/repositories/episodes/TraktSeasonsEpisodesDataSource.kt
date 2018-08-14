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

import app.tivi.data.RetrofitRunner
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.Result
import app.tivi.data.entities.Season
import app.tivi.data.mappers.EpisodeIdToTraktIdMapper
import app.tivi.data.mappers.Mapper
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktEpisodeToEpisode
import app.tivi.data.mappers.TraktHistoryItemToEpisodeWatchEntry
import app.tivi.data.mappers.TraktSeasonToSeasonWithEpisodes
import app.tivi.data.mappers.toListMapper
import app.tivi.extensions.executeWithRetry
import app.tivi.extensions.fetchBody
import com.uwetrottmann.trakt5.entities.EpisodeIds
import com.uwetrottmann.trakt5.entities.HistoryEntry
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
    private val seasonMapper: TraktSeasonToSeasonWithEpisodes,
    private val episodeMapper: TraktEpisodeToEpisode,
    private val historyItemMapper: TraktHistoryItemToEpisodeWatchEntry,
    private val retrofitRunner: RetrofitRunner
) : SeasonsEpisodesDataSource {
    override suspend fun getSeasonsEpisodes(showId: Long): Result<List<Pair<Season, List<Episode>>>> {
        val showTraktId = showIdToTraktIdMapper.map(showId)
        return retrofitRunner.executeForResponse(seasonMapper.toListMapper()) {
            seasonsService.get().summary(showTraktId.toString(), Extended.FULLEPISODES).executeWithRetry()
        }
    }

    override suspend fun getShowEpisodeWatches(showId: Long): Result<List<Pair<Episode, EpisodeWatchEntry>>> {
        val showTraktId = showIdToTraktIdMapper.map(showId)

        val mapper = object : Mapper<HistoryEntry, Pair<Episode, EpisodeWatchEntry>> {
            override fun map(from: HistoryEntry): Pair<Episode, EpisodeWatchEntry> {
                return episodeMapper.map(from.episode) to historyItemMapper.map(from)
            }
        }

        return retrofitRunner.executeForResponse(mapper.toListMapper()) {
            usersService.get().history(UserSlug.ME, HistoryType.SHOWS, showTraktId,
                    0, 10000, Extended.NOSEASONS, null, null)
                    .execute()
        }
    }

    override suspend fun getEpisodeWatches(episodeId: Long): Result<List<EpisodeWatchEntry>> {
        val episodeTraktId = episodeIdToTraktIdMapper.map(episodeId)

        return retrofitRunner.executeForResponse(historyItemMapper.toListMapper()) {
            usersService.get().history(UserSlug.ME, HistoryType.EPISODES, episodeTraktId,
                    0, 10000, Extended.NOSEASONS, null, null)
                    .execute()
        }
    }

    override suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        if (watches.isNotEmpty()) {
            val items = SyncItems()
            items.episodes = watches.mapNotNull {
                SyncEpisode().id(EpisodeIds.trakt(episodeIdToTraktIdMapper.map(it.episodeId)))
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