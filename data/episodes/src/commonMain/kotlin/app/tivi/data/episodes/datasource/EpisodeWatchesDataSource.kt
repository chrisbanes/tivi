// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes.datasource

import app.moviebase.trakt.TraktExtended
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
import app.tivi.data.mappers.map
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

interface EpisodeWatchesDataSource {

  suspend fun getShowEpisodeWatches(
    showId: Long,
    since: Instant? = null,
  ): List<Pair<Episode, EpisodeWatchEntry>>

  suspend fun getEpisodeWatches(
    episodeId: Long,
    since: Instant? = null,
  ): List<EpisodeWatchEntry>

  suspend fun getSeasonWatches(
    seasonId: Long,
    since: Instant? = null,
  ): List<Pair<Episode, EpisodeWatchEntry>>

  suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>)
  suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>)
}

@Inject
class TraktEpisodeWatchesDataSource(
  private val showIdToTraktIdMapper: ShowIdToTraktIdMapper,
  private val seasonIdToTraktIdMapper: SeasonIdToTraktIdMapper,
  private val episodeIdToTraktIdMapper: EpisodeIdToTraktIdMapper,
  private val usersService: Lazy<TraktUsersApi>,
  private val syncService: Lazy<TraktSyncApi>,
  private val episodeMapper: TraktHistoryEntryToEpisode,
  private val historyItemMapper: TraktHistoryItemToEpisodeWatchEntry,
) : EpisodeWatchesDataSource {

  private val showEpisodeWatchesMapper = pairMapperOf(episodeMapper, historyItemMapper)

  override suspend fun getShowEpisodeWatches(
    showId: Long,
    since: Instant?,
  ): List<Pair<Episode, EpisodeWatchEntry>> {
    return usersService.value.getHistory(
      itemId = showIdToTraktIdMapper.map(showId)
        ?: error("No Trakt ID for show with ID: $showId"),
      listType = TraktListMediaType.SHOWS,
      extended = TraktExtended.NO_SEASONS,
      startAt = since,
      page = 0,
      limit = 10_000,
    ).let { showEpisodeWatchesMapper(it) }
  }

  override suspend fun getSeasonWatches(
    seasonId: Long,
    since: Instant?,
  ): List<Pair<Episode, EpisodeWatchEntry>> {
    return usersService.value.getHistory(
      itemId = seasonIdToTraktIdMapper.map(seasonId),
      listType = TraktListMediaType.SEASONS,
      extended = TraktExtended.NO_SEASONS,
      startAt = since,
      page = 0,
      limit = 10_000,
    ).let { pairMapperOf(episodeMapper, historyItemMapper).invoke(it) }
  }

  override suspend fun getEpisodeWatches(
    episodeId: Long,
    since: Instant?,
  ): List<EpisodeWatchEntry> {
    return usersService.value.getHistory(
      itemId = episodeIdToTraktIdMapper.map(episodeId),
      listType = TraktListMediaType.EPISODES,
      extended = TraktExtended.NO_SEASONS,
      startAt = since,
      page = 0,
      limit = 10_000,
    ).let { historyItemMapper.map(it) }
  }

  override suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>) {
    val episodes = watches.map { watch ->
      TraktSyncEpisode(
        ids = TraktItemIds(
          trakt = episodeIdToTraktIdMapper.map(watch.episodeId),
        ),
        watchedAt = watch.watchedAt,
      )
    }
    syncService.value.addWatchedHistory(
      items = TraktSyncItems(episodes = episodes),
    )
  }

  override suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>) {
    val items = TraktSyncItems(ids = watches.mapNotNull { it.traktId })
    syncService.value.removeWatchedHistory(items)
  }
}
