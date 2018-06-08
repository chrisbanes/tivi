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

package app.tivi.tasks

import app.tivi.data.RoomTransactionRunner
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.util.AndroidLogger
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
import app.tivi.utils.syncResponse
import app.tivi.utils.testCoroutineDispatchers
import app.tivi.utils.traktHistoryEntry1
import app.tivi.utils.traktHistoryEntry2
import com.uwetrottmann.trakt5.entities.SyncItems
import com.uwetrottmann.trakt5.entities.SyncResponse
import com.uwetrottmann.trakt5.services.Sync
import com.uwetrottmann.trakt5.services.Users
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import retrofit2.Call
import retrofit2.Response
import javax.inject.Provider

class TraktEpisodeSyncerTest : BaseDatabaseTest() {
    private lateinit var episodeWatchDao: EpisodeWatchEntryDao
    private lateinit var episodeSyncer: TraktEpisodeWatchSyncer
    private lateinit var userService: Users
    private lateinit var syncService: Sync

    override fun setup() {
        super.setup()

        episodeWatchDao = db.episodeWatchesDao()
        userService = mock(Users::class.java)
        syncService = mock(Sync::class.java)

        val mockSyncResponseCall = mock(Call::class.java)
        `when`(mockSyncResponseCall.execute())
                .thenReturn(Response.success(syncResponse()))
        `when`(syncService.addItemsToWatchedHistory(any(SyncItems::class.java)))
                .thenReturn(mockSyncResponseCall as Call<SyncResponse>)
        `when`(syncService.deleteItemsFromWatchedHistory(any(SyncItems::class.java)))
                .thenReturn(mockSyncResponseCall as Call<SyncResponse>)

        episodeSyncer = TraktEpisodeWatchSyncer(
                episodeWatchDao,
                db.showDao(),
                db.episodesDao(),
                testCoroutineDispatchers,
                Provider { userService },
                Provider { syncService },
                RoomTransactionRunner(db),
                AndroidLogger
        )

        insertShow(db)
        insertSeason(db)
        insertEpisodes(db)
    }

    @Test
    fun testSync() = runBlocking {
        // Sync both watches
        episodeSyncer.syncWatchesFromTrakt(showId, listOf(traktHistoryEntry1, traktHistoryEntry2))
        // Assert that both are in the db
        assertThat(episodeWatchDao.watchesForEpisode(episodeOne.id!!),
                equalTo(listOf(episodeWatch1, episodeWatch2)))
    }

    @Test
    fun testSync_sameEntries() = runBlocking {
        // Insert both the watches
        episodeWatchDao.insertAll(episodeWatch1, episodeWatch2)
        // Now re-sync both watches
        episodeSyncer.syncWatchesFromTrakt(showId, listOf(traktHistoryEntry1, traktHistoryEntry2))
        // Assert that both are in the db
        assertThat(episodeWatchDao.watchesForEpisode(episodeOne.id!!),
                equalTo(listOf(episodeWatch1, episodeWatch2)))
    }

    @Test
    fun testSync_deletesMissing() = runBlocking {
        // Insert both the watches
        episodeWatchDao.insertAll(episodeWatch1, episodeWatch2)
        // Now sync only 1 watch
        episodeSyncer.syncWatchesFromTrakt(showId, listOf(traktHistoryEntry2))
        // Assert that only the second is in the db
        assertThat(episodeWatchDao.watchesForEpisode(episodeOne.id!!), equalTo(listOf(episodeWatch2)))
    }

    @Test
    fun testSendPendingDelete() = runBlocking {
        traktResponseForDeleteWatches()

        episodeWatchDao.insertAll(episodeWatch1, episodeWatch2PendingDelete)
        // Now sync pending deletes
        episodeSyncer.sendPendingDeleteWatchesToTrakt(showId)
        // Assert that only the pending is no longer present
        assertThat(episodeWatchDao.watchesForEpisode(episodeOne.id!!),
                equalTo(listOf(episodeWatch1)))
    }

    @Test
    fun testSendPendingSend() = runBlocking {
        traktResponseForAddWatches()

        episodeWatchDao.insertAll(episodeWatch1, episodeWatch2PendingSend)
        // Now sync pending to trakt
        episodeSyncer.sendPendingSendWatchesToTrakt(showId)
        // Assert that only the pending is no longer pending
        assertThat(episodeWatchDao.watchesForEpisode(episodeOne.id!!),
                equalTo(listOf(episodeWatch1, episodeWatch2)))
    }

    private fun traktResponseForAddWatches() {
        val mockSyncResponseCall = mock(Call::class.java)
        `when`(mockSyncResponseCall.execute())
                .thenReturn(Response.success(syncResponse()))
        `when`(syncService.addItemsToWatchedHistory(any(SyncItems::class.java)))
                .thenReturn(mockSyncResponseCall as Call<SyncResponse>)
    }

    private fun traktResponseForDeleteWatches() {
        val mockSyncResponseCall = mock(Call::class.java)
        `when`(mockSyncResponseCall.execute())
                .thenReturn(Response.success(syncResponse()))
        `when`(syncService.deleteItemsFromWatchedHistory(any(SyncItems::class.java)))
                .thenReturn(mockSyncResponseCall as Call<SyncResponse>)
    }
}