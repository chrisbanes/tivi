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

import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.entities.Success
import app.tivi.data.repositories.episodes.EpisodeDataSource
import app.tivi.data.repositories.episodes.LocalEpisodeWatchStore
import app.tivi.data.repositories.episodes.LocalSeasonsEpisodesStore
import app.tivi.data.repositories.episodes.SeasonsEpisodesDataSource
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.TestTransactionRunner
import app.tivi.utils.insertShow
import app.tivi.utils.runBlockingTest
import app.tivi.utils.s1
import app.tivi.utils.s1_episodes
import app.tivi.utils.s1_id
import app.tivi.utils.s1e1
import app.tivi.utils.s1e1w
import app.tivi.utils.s1e1w2
import app.tivi.utils.s2
import app.tivi.utils.s2_episodes
import app.tivi.utils.s2_id
import app.tivi.utils.s2e1
import app.tivi.utils.showId
import app.tivi.utils.testCoroutineDispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import javax.inject.Provider

@Ignore("https://github.com/robolectric/robolectric/issues/3556")
class SeasonsEpisodesRepositoryTest : BaseDatabaseTest() {
    private lateinit var episodeWatchDao: EpisodeWatchEntryDao
    private lateinit var seasonsDao: SeasonsDao
    private lateinit var episodesDao: EpisodesDao

    private lateinit var traktSeasonsDataSource: SeasonsEpisodesDataSource
    private lateinit var traktEpisodeDataSource: EpisodeDataSource
    private lateinit var tmdbEpisodeDataSource: EpisodeDataSource

    private lateinit var seasonEpisodeStore: LocalSeasonsEpisodesStore
    private lateinit var watchStore: LocalEpisodeWatchStore

    private lateinit var repository: SeasonsEpisodesRepository

    private var loggedInState = TraktAuthState.LOGGED_IN

    override fun setup() {
        super.setup()

        episodeWatchDao = db.episodeWatchesDao()
        episodesDao = db.episodesDao()
        seasonsDao = db.seasonsDao()

        traktSeasonsDataSource = mock(SeasonsEpisodesDataSource::class.java)
        traktEpisodeDataSource = mock(EpisodeDataSource::class.java)
        tmdbEpisodeDataSource = mock(EpisodeDataSource::class.java)

        val logger = mock(Logger::class.java)
        val txRunner = TestTransactionRunner
        val entityInserter = EntityInserter(txRunner, logger)

        watchStore = LocalEpisodeWatchStore(entityInserter, txRunner, episodeWatchDao, db.lastRequestDao(), logger)

        seasonEpisodeStore = LocalSeasonsEpisodesStore(entityInserter, txRunner, seasonsDao, episodesDao,
                db.lastRequestDao(), logger)

        repository = SeasonsEpisodesRepository(
                testCoroutineDispatchers,
                watchStore,
                seasonEpisodeStore,
                traktSeasonsDataSource,
                traktEpisodeDataSource,
                tmdbEpisodeDataSource,
                Provider { loggedInState }
        )

        // We'll assume that there's a show in the db
        runBlocking {
            insertShow(db)
        }
    }

    @Test
    fun testSyncEpisodeWatches() = runBlockingTest {
        db.seasonsDao().insert(s1)
        db.episodesDao().insertAll(s1_episodes)

        // Return a response with 2 items
        `when`(traktSeasonsDataSource.getShowEpisodeWatches(showId)).thenReturn(
                Success(listOf(s1e1 to s1e1w, s1e1 to s1e1w2))
        )
        // Sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that both are in the db
        assertThat(watchStore.getEpisodeWatchesForShow(showId), `is`(listOf(s1e1w, s1e1w2)))
    }

    @Test
    fun testEpisodeWatches_sameEntries() = runBlockingTest {
        db.seasonsDao().insert(s1)
        db.episodesDao().insertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.insertAll(s1e1w, s1e1w2)
        // Return a response with the same items
        `when`(traktSeasonsDataSource.getShowEpisodeWatches(showId))
                .thenReturn(Success(listOf(s1e1 to s1e1w, s1e1 to s1e1w2)))
        // Now re-sync with the same response
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that both are in the db
        assertThat(watchStore.getEpisodeWatchesForShow(showId), `is`(listOf(s1e1w, s1e1w2)))
    }

    @Test
    fun testEpisodeWatches_deletesMissing() = runBlockingTest {
        db.seasonsDao().insert(s1)
        db.episodesDao().insertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.insertAll(s1e1w, s1e1w2)
        // Return a response with just the second item
        `when`(traktSeasonsDataSource.getShowEpisodeWatches(showId))
                .thenReturn(Success(listOf(s1e1 to s1e1w2)))
        // Now re-sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that only the second is in the db
        assertThat(watchStore.getEpisodeWatchesForShow(showId), `is`(listOf(s1e1w2)))
    }

    @Test
    fun testEpisodeWatches_emptyResponse() = runBlockingTest {
        db.seasonsDao().insert(s1)
        db.episodesDao().insertAll(s1_episodes)

        // Insert both the watches
        episodeWatchDao.insertAll(s1e1w, s1e1w2)
        // Return a empty response
        `when`(traktSeasonsDataSource.getShowEpisodeWatches(showId)).thenReturn(Success(emptyList()))
        // Now re-sync
        repository.syncEpisodeWatchesForShow(showId)
        // Assert that the database is empty
        assertThat(watchStore.getEpisodeWatchesForShow(showId), `is`(emptyList()))
    }

    @Test
    fun testSyncSeasonsEpisodes() = runBlockingTest {
        // Return a response with 2 items
        `when`(traktSeasonsDataSource.getSeasonsEpisodes(showId))
                .thenReturn(Success(listOf(s1 to s1_episodes)))
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId), `is`(listOf(s1)))
        assertThat(episodesDao.episodesWithSeasonId(s1_id), `is`(s1_episodes))
    }

    @Test
    fun testSyncSeasonsEpisodes_sameEntries() = runBlockingTest {
        db.seasonsDao().insert(s1)
        db.episodesDao().insertAll(s1_episodes)

        // Return a response with the same items
        `when`(traktSeasonsDataSource.getSeasonsEpisodes(showId))
                .thenReturn(Success(listOf(s1 to s1_episodes)))
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId), `is`(listOf(s1)))
        assertThat(episodesDao.episodesWithSeasonId(s1_id), `is`(s1_episodes))
    }

    @Test
    fun testSyncSeasonsEpisodes_emptyResponse() = runBlockingTest {
        db.seasonsDao().insert(s1)
        db.episodesDao().insertAll(s1_episodes)

        // Return an empty response
        `when`(traktSeasonsDataSource.getSeasonsEpisodes(showId)).thenReturn(Success(emptyList()))
        repository.updateSeasonsEpisodes(showId)

        // Assert the database is empty
        assertThat(seasonsDao.seasonsForShowId(showId), `is`(emptyList()))
        assertThat(episodesDao.episodesWithSeasonId(s1_id), `is`(emptyList()))
    }

    @Test
    fun testSyncSeasonsEpisodes_deletesMissingSeasons() = runBlockingTest {
        db.seasonsDao().insertAll(s1, s2)
        db.episodesDao().insertAll(s1_episodes)
        db.episodesDao().insertAll(s2_episodes)

        // Return a response with just the first season
        `when`(traktSeasonsDataSource.getSeasonsEpisodes(showId))
                .thenReturn(Success(listOf(s1 to s1_episodes)))
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId), `is`(listOf(s1)))
        assertThat(episodesDao.episodesWithSeasonId(s1_id), `is`(s1_episodes))
    }

    @Test
    fun testSyncSeasonsEpisodes_deletesMissingEpisodes() = runBlockingTest {
        db.seasonsDao().insertAll(s1, s2)
        db.episodesDao().insertAll(s1_episodes)
        db.episodesDao().insertAll(s2_episodes)

        // Return a response with both seasons, but just a single episodes in each
        `when`(traktSeasonsDataSource.getSeasonsEpisodes(showId))
                .thenReturn(Success(listOf(s1 to listOf(s1e1), s2 to listOf(s2e1))))
        repository.updateSeasonsEpisodes(showId)

        // Assert that both are in the db
        assertThat(seasonsDao.seasonsForShowId(showId), `is`(listOf(s1, s2)))
        assertThat(episodesDao.episodesWithSeasonId(s1_id), `is`(listOf(s1e1)))
        assertThat(episodesDao.episodesWithSeasonId(s2_id), `is`(listOf(s2e1)))
    }
}