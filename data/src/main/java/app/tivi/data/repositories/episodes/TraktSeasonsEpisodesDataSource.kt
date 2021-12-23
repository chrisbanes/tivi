/*
 * Copyright 2018 Google LLC
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
import app.tivi.data.mappers.SeasonIdToTraktIdMapper
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktHistoryEntryToEpisode
import app.tivi.data.mappers.TraktHistoryItemToEpisodeWatchEntry
import app.tivi.data.mappers.TraktSeasonToSeasonWithEpisodes
import app.tivi.data.mappers.forLists
import app.tivi.data.mappers.pairMapperOf
import app.tivi.extensions.bodyOrThrow
import app.tivi.extensions.withRetry
import com.uwetrottmann.trakt5.entities.EpisodeIds
import com.uwetrottmann.trakt5.entities.SyncEpisode
import com.uwetrottmann.trakt5.entities.SyncItems
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.HistoryType
import com.uwetrottmann.trakt5.services.Seasons
import com.uwetrottmann.trakt5.services.Sync
import com.uwetrottmann.trakt5.services.Users
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import retrofit2.awaitResponse
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
) : SeasonsEpisodesDataSource {
    override suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>> {
        return withRetry {
            seasonsService.get()
                .summary(showIdToTraktIdMapper.map(showId).toString(), Extended.FULLEPISODES)
                .awaitResponse()
                .let { seasonMapper.forLists().invoke(it.bodyOrThrow()) }
        }
    }

    override suspend fun getShowEpisodeWatches(
        showId: Long,
        since: OffsetDateTime?,
    ): List<Pair<Episode, EpisodeWatchEntry>> {
        val showTraktId = showIdToTraktIdMapper.map(showId)
            ?: throw IllegalArgumentException("No Trakt ID for show with ID: $showId")

        return withRetry {
            usersService.get().history(
                UserSlug.ME,
                HistoryType.SHOWS,
                showTraktId,
                0,
                10000,
                Extended.NOSEASONS,
                since,
                null
            )
                .awaitResponse()
                .let { pairMapperOf(episodeMapper, historyItemMapper).invoke(it.bodyOrThrow()) }
        }
    }

    override suspend fun getSeasonWatches(
        seasonId: Long,
        since: OffsetDateTime?,
    ): List<Pair<Episode, EpisodeWatchEntry>> = withRetry {
        usersService.get().history(
            UserSlug.ME,
            HistoryType.SEASONS,
            seasonIdToTraktIdMapper.map(seasonId),
            0,
            10000,
            Extended.NOSEASONS,
            since,
            null
        )
            .awaitResponse()
            .let { pairMapperOf(episodeMapper, historyItemMapper).invoke(it.bodyOrThrow()) }
    }

    override suspend fun getEpisodeWatches(
        episodeId: Long,
        since: OffsetDateTime?,
    ): List<EpisodeWatchEntry> = withRetry {
        usersService.get().history(
            UserSlug.ME,
            HistoryType.EPISODES,
            episodeIdToTraktIdMapper.map(episodeId),
            0, // page
            10000, // limit
            Extended.NOSEASONS, // extended info
            since, // since date
            null // end date
        )
            .awaitResponse()
            .let { historyItemMapper.forLists().invoke(it.bodyOrThrow()) }
    }

    override suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        val items = SyncItems()
        items.episodes = watches.map {
            SyncEpisode()
                .id(EpisodeIds.trakt(episodeIdToTraktIdMapper.map(it.episodeId)))
                .watchedAt(it.watchedAt.withOffsetSameInstant(ZoneOffset.UTC))
        }
        withRetry {
            syncService.get().addItemsToWatchedHistory(items)
                .awaitResponse()
                .bodyOrThrow()
        }
    }

    override suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        val items = SyncItems()
        items.ids = watches.mapNotNull { it.traktId }
        return withRetry {
            syncService.get().deleteItemsFromWatchedHistory(items)
                .awaitResponse()
                .bodyOrThrow()
        }
    }
}
