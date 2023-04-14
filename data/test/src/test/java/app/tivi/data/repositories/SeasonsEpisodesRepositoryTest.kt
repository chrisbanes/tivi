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

import androidx.test.core.app.ApplicationProvider
import app.tivi.data.DatabaseTest
import app.tivi.data.TestApplicationComponent
import app.tivi.data.create
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.upsertAll
import app.tivi.data.episodes.EpisodeWatchStore
import app.tivi.data.episodes.SeasonsEpisodesDataSource
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.utils.AuthorizedAuthState
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
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Component
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SeasonsEpisodesRepositoryTest : DatabaseTest() {
    private lateinit var showsDao: TiviShowDao
    private lateinit var episodeWatchDao: EpisodeWatchEntryDao
    private lateinit var seasonsDao: SeasonsDao
    private lateinit var episodesDao: EpisodesDao
    private lateinit var watchStore: EpisodeWatchStore
    private lateinit var repository: SeasonsEpisodesRepository
    private lateinit var seasonsDataSource: SeasonsEpisodesDataSource
    private lateinit var traktAuthRepository: TraktAuthRepository

    @Before
    fun setup() {
        val component = SeasonsEpisodesRepositoryTestComponent::class.create()
        showsDao = component.showsDao
        episodeWatchDao = component.episodeWatchDao
        seasonsDao = component.seasonsDao
        episodesDao = component.episodesDao
        watchStore = component.watchStore
        repository = component.repository
        seasonsDataSource = component.seasonsDataSource
        traktAuthRepository = component.traktAuthRepository

        runBlocking {
            // We'll assume that there's a show in the db
            showsDao.upsert(show)
        }
    }

    @Test
    fun testSyncEpisodeWatches() = runTest {
        seasonsDao.upsert(s1)
        episodesDao.upsertAll(s1_episodes)

        // Return a response with 2 items
        coEvery { seasonsDataSource.getShowEpisodeWatches(showId) } returns
            listOf(s1e1 to s1e1w, s1e1 to s1e1w2)
        traktAuthRepository.onNewAuthState(AuthorizedAuthState)
        // Sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that both are in the db
        assertThat(watchStore.getEpisodeWatchesForShow(showId))
            .containsExactly(s1e1w, s1e1w2)
    }

    @Test
    fun testEpisodeWatches_sameEntries() = runTest {
        seasonsDao.upsert(s1)
        episodesDao.upsertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.upsertAll(s1e1w, s1e1w2)
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
    fun testEpisodeWatches_deletesMissing() = runTest {
        seasonsDao.upsert(s1)
        episodesDao.upsertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.upsertAll(s1e1w, s1e1w2)
        // Return a response with just the second item
        coEvery { seasonsDataSource.getShowEpisodeWatches(showId) } returns
            listOf(s1e1 to s1e1w2)
        traktAuthRepository.onNewAuthState(AuthorizedAuthState)
        // Now re-sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that only the second is in the db
        assertThat(watchStore.getEpisodeWatchesForShow(showId)).containsExactly(s1e1w2)
    }

    @Test
    fun testEpisodeWatches_emptyResponse() = runTest {
        seasonsDao.upsert(s1)
        episodesDao.upsertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.upsertAll(s1e1w, s1e1w2)
        // Return a empty response
        coEvery { seasonsDataSource.getShowEpisodeWatches(showId) } returns emptyList()
        traktAuthRepository.onNewAuthState(AuthorizedAuthState)
        // Now re-sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that the database is empty
        assertThat(watchStore.getEpisodeWatchesForShow(showId)).isEmpty()
    }

    @Test
    fun testSyncSeasonsEpisodes() = runTest {
        // Return a response with 2 items
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns listOf(s1 to s1_episodes)
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
    }

    @Test
    fun testSyncSeasonsEpisodes_sameEntries() = runTest {
        seasonsDao.upsert(s1)
        episodesDao.upsertAll(s1_episodes)

        // Return a response with the same items
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns listOf(s1 to s1_episodes)
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
    }

    @Test
    fun testSyncSeasonsEpisodes_emptyResponse() = runTest {
        seasonsDao.upsert(s1)
        episodesDao.upsertAll(s1_episodes)

        // Return an empty response
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns emptyList()
        repository.updateSeasonsEpisodes(showId)

        // Assert the database is empty
        assertThat(seasonsDao.seasonsForShowId(showId)).isEmpty()
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEmpty()
    }

    @Test
    fun testSyncSeasonsEpisodes_deletesMissingSeasons() = runTest {
        seasonsDao.upsertAll(s1, s2)
        episodesDao.upsertAll(s1_episodes)
        episodesDao.upsertAll(s2_episodes)

        // Return a response with just the first season
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns listOf(s1 to s1_episodes)
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1)
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).isEqualTo(s1_episodes)
    }

    @Test
    fun testSyncSeasonsEpisodes_deletesMissingEpisodes() = runTest {
        seasonsDao.upsertAll(s1, s2)
        episodesDao.upsertAll(s1_episodes)
        episodesDao.upsertAll(s2_episodes)

        // Return a response with both seasons, but just a single episodes in each
        coEvery { seasonsDataSource.getSeasonsEpisodes(showId) } returns listOf(s1 to listOf(s1e1), s2 to listOf(s2e1))
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1, s2)
        assertThat(episodesDao.episodesWithSeasonId(s1_id)).containsExactly(s1e1)
        assertThat(episodesDao.episodesWithSeasonId(s2_id)).containsExactly(s2e1)
    }

    @Test
    fun testObserveNextEpisodeToWatch_singleFlow() = runTest {
        seasonsDao.upsertAll(s1)
        episodesDao.upsertAll(s1_episodes)

        val results = repository.observeNextEpisodeToWatch(showId).produceIn(this)

        // Receive the first emission
        withTimeout(10_000) {
            assertEquals(s1e1, results.receive()?.episode)
        }

        // Now mark s1e1 as watched
        coEvery { seasonsDataSource.addEpisodeWatches(any()) } returns Unit
        coEvery { seasonsDataSource.getEpisodeWatches(s1e1.id, any()) } returns listOf(s1e1w)
        repository.addEpisodeWatch(s1e1.id, Clock.System.now())

        // Receive the second emission
        withTimeout(10_000) {
            assertEquals(s1e2, results.receive()?.episode)
        }

        results.cancel()
    }
}

@Component
abstract class SeasonsEpisodesRepositoryTestComponent(
    @Component val testApplicationComponent: TestApplicationComponent =
        TestApplicationComponent::class.create(ApplicationProvider.getApplicationContext()),
) {
    abstract val showsDao: TiviShowDao
    abstract val episodeWatchDao: EpisodeWatchEntryDao
    abstract val seasonsDao: SeasonsDao
    abstract val episodesDao: EpisodesDao
    abstract val watchStore: EpisodeWatchStore
    abstract val repository: SeasonsEpisodesRepository
    abstract val seasonsDataSource: SeasonsEpisodesDataSource
    abstract val traktAuthRepository: TraktAuthRepository
}
