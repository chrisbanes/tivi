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

package app.tivi.data.repositories

import app.tivi.data.DatabaseModuleBinds
import app.tivi.data.DatabaseTest
import app.tivi.data.TiviDatabase
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.repositories.episodes.EpisodeDataSourceBinds
import app.tivi.data.repositories.episodes.EpisodeWatchStore
import app.tivi.data.repositories.episodes.SeasonsEpisodesDataSource
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.utils.insertShow
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
import app.tivi.utils.showId
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

@UninstallModules(DatabaseModuleBinds::class, EpisodeDataSourceBinds::class)
@HiltAndroidTest
class SeasonsEpisodesRepositoryTest : DatabaseTest() {
    @Inject lateinit var database: TiviDatabase
    @Inject lateinit var episodeWatchDao: EpisodeWatchEntryDao
    @Inject lateinit var seasonsDao: SeasonsDao
    @Inject lateinit var episodesDao: EpisodesDao
    @Inject lateinit var watchStore: EpisodeWatchStore
    @Inject lateinit var repository: SeasonsEpisodesRepository
    @Inject lateinit var seasonsDataSource: SeasonsEpisodesDataSource

    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            // We'll assume that there's a show in the db
            insertShow(database)
        }
    }

    @Test
    fun testSyncEpisodeWatches() = testScope.runBlockingTest {
        seasonsDao.insert(s1)
        episodesDao.insertAll(s1_episodes)

        // Return a response with 2 items
        coEvery { seasonsDataSource.getShowEpisodeWatches(showId) } returns
            listOf(s1e1 to s1e1w, s1e1 to s1e1w2)
        // Sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that both are in the db
        assertThat(watchStore.getEpisodeWatchesForShow(showId))
            .containsExactly(s1e1w, s1e1w2)
    }

    @Test
    fun testEpisodeWatches_sameEntries() = testScope.runBlockingTest {
        seasonsDao.insert(s1)
        episodesDao.insertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.insertAll(s1e1w, s1e1w2)
        // Return a response with the same items
        coEvery { seasonsDataSource.getShowEpisodeWatches(showId) } returns
            listOf(s1e1 to s1e1w, s1e1 to s1e1w2)
        // Now re-sync with the same response
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that both are in the db
        assertThat(watchStore.getEpisodeWatchesForShow(showId))
            .containsExactly(s1e1w, s1e1w2)
    }

    @Test
    fun testEpisodeWatches_deletesMissing() = testScope.runBlockingTest {
        seasonsDao.insert(s1)
        episodesDao.insertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.insertAll(s1e1w, s1e1w2)
        // Return a response with just the second item
        coEvery { seasonsDataSource.getShowEpisodeWatches(showId) } returns
            listOf(s1e1 to s1e1w2)
        // Now re-sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that only the second is in the db
        assertThat(watchStore.getEpisodeWatchesForShow(showId)).containsExactly(s1e1w2)
    }

    @Test
    fun testEpisodeWatches_emptyResponse() = testScope.runBlockingTest {
        seasonsDao.insert(s1)
        episodesDao.insertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.insertAll(s1e1w, s1e1w2)
        // Return a empty response
        coEvery { seasonsDataSource.getShowEpisodeWatches(showId) } returns emptyList()
        // Now re-sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that the database is empty
        assertThat(watchStore.getEpisodeWatchesForShow(showId)).isEmpty()
    }

    @Test
    fun testSyncSeasonsEpisodes() = testScope.runBlockingTest {
        // Return a response with 2 items
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns listOf(s1 to s1_episodes)
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
    }

    @Test
    fun testSyncSeasonsEpisodes_sameEntries() = testScope.runBlockingTest {
        seasonsDao.insert(s1)
        episodesDao.insertAll(s1_episodes)

        // Return a response with the same items
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns listOf(s1 to s1_episodes)
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
    }

    @Test
    fun testSyncSeasonsEpisodes_emptyResponse() = testScope.runBlockingTest {
        seasonsDao.insert(s1)
        episodesDao.insertAll(s1_episodes)

        // Return an empty response
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns emptyList()
        repository.updateSeasonsEpisodes(showId)

        // Assert the database is empty
        assertThat(seasonsDao.seasonsForShowId(showId)).isEmpty()
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEmpty()
    }

    @Test
    fun testSyncSeasonsEpisodes_deletesMissingSeasons() = testScope.runBlockingTest {
        seasonsDao.insertAll(s1, s2)
        episodesDao.insertAll(s1_episodes)
        episodesDao.insertAll(s2_episodes)

        // Return a response with just the first season
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns listOf(s1 to s1_episodes)
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
    }

    @Test
    fun testSyncSeasonsEpisodes_deletesMissingEpisodes() = testScope.runBlockingTest {
        seasonsDao.insertAll(s1, s2)
        episodesDao.insertAll(s1_episodes)
        episodesDao.insertAll(s2_episodes)

        // Return a response with both seasons, but just a single episodes in each
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns listOf(s1 to listOf(s1e1), s2 to listOf(s2e1))
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1, s2)
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).containsExactly(s1e1)
        assertThat(episodesDao.episodesWithSeasonId(s2_id)).containsExactly(s2e1)
    }

    @Test
    fun testObserveNextEpisodeToWatch_singleFlow() = testScope.runBlockingTest {
        seasonsDao.insertAll(s1)
        episodesDao.insertAll(s1_episodes)

        val results = repository.observeNextEpisodeToWatch(showId).produceIn(this)

        // Receive the first emission
        withTimeout(10_000) {
            assertEquals(s1e1, results.receive()?.episode)
        }

        // Now mark s1e1 as watched
        coEvery { seasonsDataSource.addEpisodeWatches(any()) } returns Unit
        coEvery { seasonsDataSource.getEpisodeWatches(s1e1.id, any()) } returns listOf(s1e1w)
        repository.addEpisodeWatch(s1e1.id, OffsetDateTime.now())

        // Receive the second emission
        withTimeout(10_000) {
            assertEquals(s1e2, results.receive()?.episode)
        }

        results.cancel()
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
