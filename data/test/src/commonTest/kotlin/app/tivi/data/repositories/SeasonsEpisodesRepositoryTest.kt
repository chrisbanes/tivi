// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.repositories

import app.cash.turbine.test
import app.tivi.data.DatabaseTest
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.insert
import app.tivi.data.episodes.EpisodeWatchStore
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.utils.ObjectGraph
import app.tivi.utils.createSingleAppCoroutineDispatchers
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock

class SeasonsEpisodesRepositoryTest : DatabaseTest() {
  private val testScope = TestScope()

  private val objectGraph by lazy {
    ObjectGraph(
      database = database,
      backgroundScope = testScope.backgroundScope,
      appCoroutineDispatchers = createSingleAppCoroutineDispatchers(StandardTestDispatcher(testScope.testScheduler)),
    )
  }

  private val showsDao: TiviShowDao get() = objectGraph.tiviShowDao
  private val episodesDao: EpisodesDao get() = objectGraph.episodesDao
  private val seasonsDao: SeasonsDao get() = objectGraph.seasonsDao
  private val episodeWatchDao: EpisodeWatchEntryDao get() = objectGraph.episodeWatchEntryDao

  private val watchStore: EpisodeWatchStore get() = objectGraph.episodeWatchStore
  private val repository: SeasonsEpisodesRepository get() = objectGraph.seasonsEpisodesRepository

  private val traktAuthRepository: TraktAuthRepository get() = objectGraph.traktAuthRepository
  private val traktSeasonsEpisodesDataSource get() = objectGraph.traktSeasonsEpisodesDataSource
  private val episodeWatchesDataSource get() = objectGraph.episodeWatchesDataSource

  @BeforeTest
  fun setup() {
    // We'll assume that there's a show in the db
    showsDao.insert(show)
  }

  @Test
  fun testSyncEpisodeWatches() = testScope.runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Return a response with 2 items
    episodeWatchesDataSource.getShowEpisodeWatchesResult =
      Result.success(listOf(s1e1 to s1e1w, s1e1 to s1e1w2))
    traktAuthRepository.login()
    // Sync
    repository.syncEpisodeWatchesForShow(showId)
    // Assert that both are in the db
    assertThat(watchStore.getEpisodeWatchesForShow(showId))
      .containsExactly(s1e1w, s1e1w2)
  }

  @Test
  fun testEpisodeWatches_sameEntries() = testScope.runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Insert both the watches
    episodeWatchDao.insert(s1e1w, s1e1w2)
    // Return a response with the same items
    episodeWatchesDataSource.getShowEpisodeWatchesResult =
      Result.success(listOf(s1e1 to s1e1w, s1e1 to s1e1w2))
    // Now re-sync with the same response
    repository.syncEpisodeWatchesForShow(showId)
    // Assert that both are in the db
    assertThat(watchStore.getEpisodeWatchesForShow(showId))
      .containsExactly(s1e1w, s1e1w2)
  }

  @Test
  fun testEpisodeWatches_deletesMissing() = testScope.runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Insert both the watches
    episodeWatchDao.insert(s1e1w, s1e1w2)
    // Return a response with just the second item
    episodeWatchesDataSource.getShowEpisodeWatchesResult = Result.success(listOf(s1e1 to s1e1w2))

    traktAuthRepository.login()
    // Now re-sync
    repository.syncEpisodeWatchesForShow(showId)
    // Assert that only the second is in the db
    assertThat(watchStore.getEpisodeWatchesForShow(showId))
      .containsExactly(s1e1w2)
  }

  @Test
  fun testEpisodeWatches_emptyResponse() = testScope.runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Insert both the watches
    episodeWatchDao.insert(s1e1w, s1e1w2)
    // Return a empty response
    episodeWatchesDataSource.getShowEpisodeWatchesResult = Result.success(emptyList())
    traktAuthRepository.login()
    // Now re-sync
    repository.syncEpisodeWatchesForShow(showId)
    // Assert that the database is empty
    assertThat(watchStore.getEpisodeWatchesForShow(showId)).isEmpty()
  }

  @Test
  fun testSyncSeasonsEpisodes() = testScope.runTest {
    // Return a response with 2 items

    traktSeasonsEpisodesDataSource.getSeasonsEpisodesResult = Result.success(listOf(s1 to s1_episodes))
    repository.updateSeasonsEpisodes(showId)

    // Assert that both are in the db
    assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
  }

  @Test
  fun testSyncSeasonsEpisodes_sameEntries() = testScope.runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Return a response with the same items
    traktSeasonsEpisodesDataSource.getSeasonsEpisodesResult = Result.success(listOf(s1 to s1_episodes))
    repository.updateSeasonsEpisodes(showId)

    // Assert that both are in the db
    assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
  }

  @Test
  fun testSyncSeasonsEpisodes_emptyResponse() = testScope.runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    // Return an empty response
    traktSeasonsEpisodesDataSource.getSeasonsEpisodesResult = Result.success(emptyList())
    repository.updateSeasonsEpisodes(showId)

    // Assert the database is empty
    assertThat(seasonsDao.seasonsForShowId(showId)).isEmpty()
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEmpty()
  }

  @Test
  fun testSyncSeasonsEpisodes_deletesMissingSeasons() = testScope.runTest {
    seasonsDao.insert(s1, s2)
    episodesDao.insert(s1_episodes)
    episodesDao.insert(s2_episodes)

    // Return a response with just the first season
    traktSeasonsEpisodesDataSource.getSeasonsEpisodesResult = Result.success(listOf(s1 to s1_episodes))
    repository.updateSeasonsEpisodes(showId)

    // Assert that both are in the db
    assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
  }

  @Test
  fun testSyncSeasonsEpisodes_deletesMissingEpisodes() = testScope.runTest {
    seasonsDao.insert(s1, s2)
    episodesDao.insert(s1_episodes)
    episodesDao.insert(s2_episodes)

    // Return a response with both seasons, but just a single episodes in each
    traktSeasonsEpisodesDataSource.getSeasonsEpisodesResult =
      Result.success(listOf(s1 to listOf(s1e1), s2 to listOf(s2e1)))
    repository.updateSeasonsEpisodes(showId)

    // Assert that both are in the db
    assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1, s2)
    assertThat(episodesDao.episodesWithSeasonId(s1_id)).containsExactly(s1e1)
    assertThat(episodesDao.episodesWithSeasonId(s2_id)).containsExactly(s2e1)
  }

  @Test
  fun testObserveNextEpisodeToWatch_singleFlow() = testScope.runTest {
    seasonsDao.insert(s1)
    episodesDao.insert(s1_episodes)

    repository.observeNextEpisodeToWatch(showId).test {
      assertThat(awaitItem()?.episode).isEqualTo(s1e1)

      // Now mark s1e1 as watched
      episodeWatchesDataSource.addEpisodeWatchesResult = Result.success(Unit)
      episodeWatchesDataSource.getEpisodeWatchesResult = Result.success(listOf(s1e1w))
      repository.addEpisodeWatch(s1e1.id, Clock.System.now())

      assertThat(awaitItem()?.episode).isEqualTo(s1e2)
    }
  }
}
