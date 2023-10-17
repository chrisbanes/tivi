// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.repositories

import app.cash.turbine.test
import app.tivi.data.DatabaseTest
import app.tivi.data.TestApplicationComponent
import app.tivi.data.create
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.insert
import app.tivi.data.episodes.EpisodeWatchStore
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.episodes.TmdbSeasonsEpisodesDataSource
import app.tivi.data.episodes.TraktSeasonsEpisodesDataSource
import app.tivi.data.episodes.datasource.EpisodeWatchesDataSource
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.utils.FakeEpisodeWatchesDataSource
import app.tivi.utils.FakeSeasonsEpisodesDataSource
import app.tivi.utils.s1
import app.tivi.utils.s1_episodes
import app.tivi.utils.s1_id
import app.tivi.utils.s1e1
import app.tivi.utils.s1e1w
import app.tivi.utils.s1e1w2
import app.tivi.utils.s1e2
import app.tivi.utils.s2
import app.tivi.utils.s2_episodes
import app.tivi.utils.s2_id
import app.tivi.utils.s2e1
import app.tivi.utils.show
import app.tivi.utils.showId
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Component

class SeasonsEpisodesRepositoryTest : DatabaseTest() {
  private lateinit var showsDao: TiviShowDao
  private lateinit var episodeWatchDao: EpisodeWatchEntryDao
  private lateinit var seasonsDao: SeasonsDao
  private lateinit var episodesDao: EpisodesDao
  private lateinit var watchStore: EpisodeWatchStore
  private lateinit var repository: SeasonsEpisodesRepository
  private lateinit var traktSeasonDataSource: FakeSeasonsEpisodesDataSource
  private lateinit var tmdbSeasonDataSource: FakeSeasonsEpisodesDataSource
  private lateinit var watchesDataSource: FakeEpisodeWatchesDataSource
  private lateinit var traktAuthRepository: TraktAuthRepository

  @BeforeTest
  fun setup() {
    val component = SeasonsEpisodesRepositoryTestComponent::class.create(applicationComponent)
    showsDao = component.showsDao
    episodeWatchDao = component.episodeWatchDao
    seasonsDao = component.seasonsDao
    episodesDao = component.episodesDao
    watchStore = component.watchStore
    repository = component.repository
    traktSeasonDataSource = component.traktSeasonsDataSource as FakeSeasonsEpisodesDataSource
    tmdbSeasonDataSource = component.tmdbSeasonsDataSource as FakeSeasonsEpisodesDataSource
    watchesDataSource = component.episodeWatchesDataSource as FakeEpisodeWatchesDataSource
    traktAuthRepository = component.traktAuthRepository

    // We'll assume that there's a show in the db
    showsDao.insert(show)
  }

  @Test
  fun testSyncEpisodeWatches() = runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Return a response with 2 items
    watchesDataSource.getShowEpisodeWatchesResult =
      Result.success(listOf(s1e1 to s1e1w, s1e1 to s1e1w2))
    traktAuthRepository.login()
    // Sync
    repository.syncEpisodeWatchesForShow(showId)
    // Assert that both are in the db
    assertThat(watchStore.getEpisodeWatchesForShow(showId))
      .containsExactly(s1e1w, s1e1w2)
  }

  @Test
  fun testEpisodeWatches_sameEntries() = runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Insert both the watches
    episodeWatchDao.insert(s1e1w, s1e1w2)
    // Return a response with the same items
    watchesDataSource.getShowEpisodeWatchesResult =
      Result.success(listOf(s1e1 to s1e1w, s1e1 to s1e1w2))
    // Now re-sync with the same response
    repository.syncEpisodeWatchesForShow(showId)
    // Assert that both are in the db
    assertThat(watchStore.getEpisodeWatchesForShow(showId))
      .containsExactly(s1e1w, s1e1w2)
  }

  @Test
  fun testEpisodeWatches_deletesMissing() = runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Insert both the watches
    episodeWatchDao.insert(s1e1w, s1e1w2)
    // Return a response with just the second item
    watchesDataSource.getShowEpisodeWatchesResult = Result.success(listOf(s1e1 to s1e1w2))

    traktAuthRepository.login()
    // Now re-sync
    repository.syncEpisodeWatchesForShow(showId)
    // Assert that only the second is in the db
    assertThat(watchStore.getEpisodeWatchesForShow(showId))
      .containsExactly(s1e1w2)
  }

  @Test
  fun testEpisodeWatches_emptyResponse() = runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Insert both the watches
    episodeWatchDao.insert(s1e1w, s1e1w2)
    // Return a empty response
    watchesDataSource.getShowEpisodeWatchesResult = Result.success(emptyList())
    traktAuthRepository.login()
    // Now re-sync
    repository.syncEpisodeWatchesForShow(showId)
    // Assert that the database is empty
    assertThat(watchStore.getEpisodeWatchesForShow(showId)).isEmpty()
  }

  @Test
  fun testSyncSeasonsEpisodes() = runTest {
    // Return a response with 2 items

    traktSeasonDataSource.getSeasonsEpisodesResult = Result.success(listOf(s1 to s1_episodes))
    repository.updateSeasonsEpisodes(showId)

    // Assert that both are in the db
    assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
  }

  @Test
  fun testSyncSeasonsEpisodes_sameEntries() = runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Return a response with the same items
    traktSeasonDataSource.getSeasonsEpisodesResult = Result.success(listOf(s1 to s1_episodes))
    repository.updateSeasonsEpisodes(showId)

    // Assert that both are in the db
    assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
  }

  @Test
  fun testSyncSeasonsEpisodes_emptyResponse() = runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Return an empty response
    traktSeasonDataSource.getSeasonsEpisodesResult = Result.success(emptyList())
    repository.updateSeasonsEpisodes(showId)

    // Assert the database is empty
    assertThat(seasonsDao.seasonsForShowId(showId)).isEmpty()
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEmpty()
  }

  @Test
  fun testSyncSeasonsEpisodes_deletesMissingSeasons() = runTest {
    seasonsDao.insert(s1, s2)
    episodesDao.insert(s1_episodes)
    episodesDao.insert(s2_episodes)

    // Return a response with just the first season
    traktSeasonDataSource.getSeasonsEpisodesResult = Result.success(listOf(s1 to s1_episodes))
    repository.updateSeasonsEpisodes(showId)

    // Assert that both are in the db
    assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
  }

  @Test
  fun testSyncSeasonsEpisodes_deletesMissingEpisodes() = runTest {
    seasonsDao.insert(s1, s2)
    episodesDao.insert(s1_episodes)
    episodesDao.insert(s2_episodes)

    // Return a response with both seasons, but just a single episodes in each
    traktSeasonDataSource.getSeasonsEpisodesResult =
      Result.success(listOf(s1 to listOf(s1e1), s2 to listOf(s2e1)))
    repository.updateSeasonsEpisodes(showId)

    // Assert that both are in the db
    assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1, s2)
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).containsExactly(s1e1)
    assertThat(episodesDao.episodesWithSeasonId(s2_id)).containsExactly(s2e1)
  }

  @Test
  fun testObserveNextEpisodeToWatch_singleFlow() = runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    repository.observeNextEpisodeToWatch(showId).test {
      assertThat(awaitItem()?.episode).isEqualTo(s1e1)

      // Now mark s1e1 as watched
      watchesDataSource.addEpisodeWatchesResult = Result.success(Unit)
      watchesDataSource.getEpisodeWatchesResult = Result.success(listOf(s1e1w))
      repository.addEpisodeWatch(s1e1.id, Clock.System.now())

      assertThat(awaitItem()?.episode).isEqualTo(s1e2)
    }
  }
}

@Component
abstract class SeasonsEpisodesRepositoryTestComponent(
  @Component val applicationComponent: TestApplicationComponent,
) {
  abstract val showsDao: TiviShowDao
  abstract val episodeWatchDao: EpisodeWatchEntryDao
  abstract val seasonsDao: SeasonsDao
  abstract val episodesDao: EpisodesDao
  abstract val watchStore: EpisodeWatchStore
  abstract val repository: SeasonsEpisodesRepository
  abstract val traktSeasonsDataSource: TraktSeasonsEpisodesDataSource
  abstract val tmdbSeasonsDataSource: TmdbSeasonsEpisodesDataSource
  abstract val episodeWatchesDataSource: EpisodeWatchesDataSource
  abstract val traktAuthRepository: TraktAuthRepository
}
