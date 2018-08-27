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
import app.tivi.data.mappers.SeasonIdToTraktIdMapper
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktHistoryEntryToEpisode
import app.tivi.data.mappers.TraktHistoryItemToEpisodeWatchEntry
import app.tivi.data.mappers.TraktSeasonToSeasonWithEpisodes
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.mappers.toListMapper
import app.tivi.extensions.executeWithRetry
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
    private val seasonIdToTraktIdMapper: SeasonIdToTraktIdMapper,
    private val episodeIdToTraktIdMapper: EpisodeIdToTraktIdMapper,
    private val seasonsService: Provider<Seasons>,
    private val usersService: Provider<Users>,
    private val syncService: Provider<Sync>,
    private val seasonMapper: TraktSeasonToSeasonWithEpisodes,
    private val episodeMapper: TraktHistoryEntryToEpisode,
    private val historyItemMapper: TraktHistoryItemToEpisodeWatchEntry,
    private val retrofitRunner: RetrofitRunner
) : SeasonsEpisodesDataSource {
    override suspend fun getSeasonsEpisodes(showId: Long): Result<List<Pair<Season, List<Episode>>>> {
        return retrofitRunner.executeForResponse(seasonMapper.toListMapper()) {
            seasonsService.get().summary(showIdToTraktIdMapper.map(showId).toString(), Extended.FULLEPISODES)
                    .executeWithRetry()
        }
    }

    override suspend fun getShowEpisodeWatches(showId: Long): Result<List<Pair<Episode, EpisodeWatchEntry>>> {
        return retrofitRunner.executeForResponse(pairMapperOf(episodeMapper, historyItemMapper)) {
            usersService.get().history(UserSlug.ME, HistoryType.SHOWS, showIdToTraktIdMapper.map(showId),
                    0, 10000, Extended.NOSEASONS, null, null)
                    .executeWithRetry()
        }
    }

    override suspend fun getSeasonWatches(seasonId: Long): Result<List<Pair<Episode, EpisodeWatchEntry>>> {
        return retrofitRunner.executeForResponse(pairMapperOf(episodeMapper, historyItemMapper)) {
            usersService.get().history(UserSlug.ME, HistoryType.SEASONS, seasonIdToTraktIdMapper.map(seasonId),
                    0, 10000, Extended.NOSEASONS, null, null)
                    .executeWithRetry()
        }
    }

    override suspend fun getEpisodeWatches(episodeId: Long): Result<List<EpisodeWatchEntry>> {
        return retrofitRunner.executeForResponse(historyItemMapper.toListMapper()) {
            usersService.get().history(UserSlug.ME, HistoryType.EPISODES, episodeIdToTraktIdMapper.map(episodeId),
                    0, 10000, Extended.NOSEASONS, null, null)
                    .executeWithRetry()
        }
    }

    override suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>): Result<Unit> {
        return retrofitRunner.executeForResponse {
            val items = SyncItems()
            items.episodes = watches.map {
                SyncEpisode().id(EpisodeIds.trakt(episodeIdToTraktIdMapper.map(it.episodeId)))
            }
            syncService.get().addItemsToWatchedHistory(items).executeWithRetry()
        }
    }

    override suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>): Result<Unit> {
        return retrofitRunner.executeForResponse {
            val items = SyncItems()
            items.ids = watches.mapNotNull { it.traktId }

            syncService.get().deleteItemsFromWatchedHistory(items).executeWithRetry()
        }
    }
}