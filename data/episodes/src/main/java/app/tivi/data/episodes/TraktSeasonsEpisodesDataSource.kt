/*
 * Copyright 2023 Google LLC
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

package app.tivi.data.episodes

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktSeasonsApi
import app.moviebase.trakt.api.TraktSyncApi
import app.moviebase.trakt.api.TraktUsersApi
import app.moviebase.trakt.model.TraktItemIds
import app.moviebase.trakt.model.TraktListMediaType
import app.moviebase.trakt.model.TraktSyncEpisode
import app.moviebase.trakt.model.TraktSyncItems
import app.tivi.data.mappers.EpisodeIdToTraktIdMapper
import app.tivi.data.mappers.SeasonIdToTraktIdMapper
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktHistoryEntryToEpisode
import app.tivi.data.mappers.TraktHistoryItemToEpisodeWatchEntry
import app.tivi.data.mappers.TraktSeasonToSeasonWithEpisodes
import app.tivi.data.mappers.map
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.Season
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@Inject
class TraktSeasonsEpisodesDataSource(
    private val showIdToTraktIdMapper: ShowIdToTraktIdMapper,
    private val seasonIdToTraktIdMapper: SeasonIdToTraktIdMapper,
    private val episodeIdToTraktIdMapper: EpisodeIdToTraktIdMapper,
    private val seasonsService: Lazy<TraktSeasonsApi>,
    private val usersService: Lazy<TraktUsersApi>,
    private val syncService: Lazy<TraktSyncApi>,
    private val seasonMapper: TraktSeasonToSeasonWithEpisodes,
    private val episodeMapper: TraktHistoryEntryToEpisode,
    private val historyItemMapper: TraktHistoryItemToEpisodeWatchEntry,
) : SeasonsEpisodesDataSource {

    private val showEpisodeWatchesMapper = pairMapperOf(episodeMapper, historyItemMapper)

    override suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>> {
        val showTraktId = showIdToTraktIdMapper.map(showId)
        requireNotNull(showTraktId) { "No Trakt ID for show with ID: $showId" }

        return seasonsService.value
            .getSummary(showId = showTraktId.toString(), extended = TraktExtended.EPISODES)
            .let { seasonMapper.map(it) }
    }

    override suspend fun getShowEpisodeWatches(
        showId: Long,
        since: Instant?,
    ): List<Pair<Episode, EpisodeWatchEntry>> {
        val showTraktId = showIdToTraktIdMapper.map(showId)
        requireNotNull(showTraktId) { "No Trakt ID for show with ID: $showId" }

        return usersService.value
            .getHistory(
                listType = TraktListMediaType.SHOWS,
                extended = TraktExtended.NOSEASONS,
                startAt = since,
            ).let { showEpisodeWatchesMapper(it) }
    }

    override suspend fun getSeasonWatches(
        seasonId: Long,
        since: Instant?,
    ): List<Pair<Episode, EpisodeWatchEntry>> =
        usersService.value
            .getHistory(
                listType = TraktListMediaType.SEASONS,
                extended = TraktExtended.NOSEASONS,
                itemId = seasonIdToTraktIdMapper.map(seasonId),
                startAt = since,
            )
            .let { pairMapperOf(episodeMapper, historyItemMapper).invoke(it) }

    override suspend fun getEpisodeWatches(
        episodeId: Long,
        since: Instant?,
    ): List<EpisodeWatchEntry> =
        usersService.value
            .getHistory(
                listType = TraktListMediaType.EPISODES,
                extended = TraktExtended.NOSEASONS,
                itemId = episodeIdToTraktIdMapper.map(episodeId),
                startAt = since,
            )
            .let { historyItemMapper.map(it) }

    override suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        val episodes = watches.map {
            val traktEpisodeId = episodeIdToTraktIdMapper.map(it.episodeId)
            TraktSyncEpisode(
                ids = TraktItemIds(trakt = traktEpisodeId),
                watchedAt = it.watchedAt
            )
        }
        val items = TraktSyncItems(episodes = episodes)

        syncService.value.addWatchedHistory(items)
    }

    override suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        val items = TraktSyncItems(ids = watches.mapNotNull { it.traktId })
        syncService.value.removeWatchedHistory(items)
    }
}
