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
import app.tivi.utils.BaseTest
import app.tivi.utils.episodeOne
import app.tivi.utils.episodeWatch1
import app.tivi.utils.episodeWatch2
import app.tivi.utils.insertEpisodes
import app.tivi.utils.insertSeason
import app.tivi.utils.insertShow
import app.tivi.utils.testCoroutineDispatchers
import app.tivi.utils.traktHistoryEntry1
import app.tivi.utils.traktHistoryEntry2
import com.uwetrottmann.trakt5.services.Sync
import com.uwetrottmann.trakt5.services.Users
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import javax.inject.Provider

class TraktEpisodeSyncerTest : BaseTest() {
    private lateinit var episodeWatchDao: EpisodeWatchEntryDao
    private lateinit var episodeSyncer: TraktEpisodeWatchSyncer
    private lateinit var userService: Users
    private lateinit var syncService: Sync

    override fun setup() {
        super.setup()

        episodeWatchDao = db.episodeWatchesDao()

        userService = mock(Users::class.java)

        syncService = mock(Sync::class.java)

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
    }

    @Test
    fun testSyncWatch() = runBlocking {
        insertShow(db)
        insertSeason(db)
        insertEpisodes(db)
        // Now sync watch
        episodeSyncer.syncWatchesFromTrakt(listOf(traktHistoryEntry1, traktHistoryEntry2))

        assertThat(episodeWatchDao.watchesForEpisode(episodeOne.id!!),
                equalTo(listOf(episodeWatch1, episodeWatch2)))
    }
}