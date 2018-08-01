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

package app.tivi.data.repositories

import app.tivi.data.RoomTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.repositories.episodes.EpisodeDataSource
import app.tivi.data.repositories.episodes.LocalSeasonsEpisodesStore
import app.tivi.data.repositories.episodes.SeasonsEpisodesDataSource
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.trakt.TraktAuthState
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.episodeOne
import app.tivi.utils.episodeWatch1
import app.tivi.utils.episodeWatch2
import app.tivi.utils.episodeWatch2PendingDelete
import app.tivi.utils.episodeWatch2PendingSend
import app.tivi.utils.insertEpisodes
import app.tivi.utils.insertSeason
import app.tivi.utils.insertShow
import app.tivi.utils.showId
import app.tivi.utils.testCoroutineDispatchers
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import javax.inject.Provider

class SeasonsEpisodesRepositoryTest : BaseDatabaseTest() {

    private lateinit var episodeWatchDao: EpisodeWatchEntryDao

    private lateinit var traktSeasonsDataSource: SeasonsEpisodesDataSource
    private lateinit var traktEpisodeDataSource: EpisodeDataSource
    private lateinit var tmdbEpisodeDataSource: EpisodeDataSource

    private lateinit var localEpisodeStore: LocalSeasonsEpisodesStore

    private lateinit var repository: SeasonsEpisodesRepository

    private var loggedInState = TraktAuthState.LOGGED_IN

    override fun setup() {
        super.setup()

        episodeWatchDao = db.episodeWatchesDao()

        traktSeasonsDataSource = mock(SeasonsEpisodesDataSource::class.java)
        traktEpisodeDataSource = mock(EpisodeDataSource::class.java)
        tmdbEpisodeDataSource = mock(EpisodeDataSource::class.java)

        val txRunner = RoomTransactionRunner(db)

        localEpisodeStore = LocalSeasonsEpisodesStore(EntityInserter(txRunner), txRunner,
                db.seasonsDao(), db.episodesDao(), episodeWatchDao)

        repository = SeasonsEpisodesRepository(
                testCoroutineDispatchers,
                localEpisodeStore,
                traktSeasonsDataSource,
                traktEpisodeDataSource,
                tmdbEpisodeDataSource,
                Provider { loggedInState }
        )

        // We'll assume that there's a show and season in the db
        insertShow(db)
        insertSeason(db)
        insertEpisodes(db)
    }

    @Test
    fun testSyncEpisodeWatches() = runBlocking {
        // Return a response with 2 items
        `when`(traktSeasonsDataSource.getShowEpisodeWatches(showId)).thenReturn(
                listOf(episodeOne to episodeWatch1, episodeOne to episodeWatch2)
        )
        // Sync
        repository.syncEpisodeWatches(showId)
        // Assert that both are in the db
        assertThat(localEpisodeStore.getEpisodeWatches(showId), `is`(listOf(episodeWatch1, episodeWatch2)))
    }

    @Test
    fun testSync_sameEntries() = runBlocking {
        // Insert both the watches
        episodeWatchDao.insertAll(episodeWatch1, episodeWatch2)
        // Return a response with the same items
        `when`(traktSeasonsDataSource.getShowEpisodeWatches(showId))
                .thenReturn(listOf(episodeOne to episodeWatch1, episodeOne to episodeWatch2))
        // Now re-sync with the same response
        repository.syncEpisodeWatches(showId)
        // Assert that both are in the db
        assertThat(localEpisodeStore.getEpisodeWatches(showId), `is`(listOf(episodeWatch1, episodeWatch2)))
    }

    @Test
    fun testSync_deletesMissing() = runBlocking {
        // Insert both the watches
        episodeWatchDao.insertAll(episodeWatch1, episodeWatch2)
        // Return a response with just the second item
        `when`(traktSeasonsDataSource.getShowEpisodeWatches(showId))
                .thenReturn(listOf(episodeOne to episodeWatch2))
        // Now re-sync
        repository.syncEpisodeWatches(showId)
        // Assert that only the second is in the db
        assertThat(localEpisodeStore.getEpisodeWatches(showId), `is`(listOf(episodeWatch2)))
    }

    @Test
    fun testSync_emptyResponse() = runBlocking {
        // Insert both the watches
        episodeWatchDao.insertAll(episodeWatch1, episodeWatch2)
        // Return a empty response
        `when`(traktSeasonsDataSource.getShowEpisodeWatches(showId)).thenReturn(emptyList())
        // Now re-sync
        repository.syncEpisodeWatches(showId)
        // Assert that the database is empty
        assertThat(localEpisodeStore.getEpisodeWatches(showId), `is`(emptyList()))
    }

    @Test
    fun testSync_pendingDelete() = runBlocking {
        loggedInState = TraktAuthState.LOGGED_OUT

        episodeWatchDao.insert(episodeWatch2PendingDelete)
        // Now re-sync
        repository.syncEpisodeWatches(showId)
        // Assert that the database is empty
        assertThat(localEpisodeStore.getEpisodeWatches(showId), `is`(emptyList()))
    }

    @Test
    fun testSync_pendingAdd() = runBlocking {
        loggedInState = TraktAuthState.LOGGED_OUT

        episodeWatchDao.insert(episodeWatch2PendingSend)
        // Now re-sync
        repository.syncEpisodeWatches(showId)
        // Assert that the database has episode watch 2 (not pending)
        assertThat(localEpisodeStore.getEpisodeWatches(showId), `is`(listOf(episodeWatch2)))
    }
}