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

package app.tivi.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.tivi.data.DaggerTestComponent
import app.tivi.data.TestDataSourceModule
import app.tivi.data.TiviDatabase
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.utils.episodeWatch2PendingDelete
import app.tivi.utils.episodeWatch2PendingSend
import app.tivi.utils.insertShow
import app.tivi.utils.s1
import app.tivi.utils.s1_episodes
import app.tivi.utils.s1e1
import app.tivi.utils.s1e1w
import app.tivi.utils.s1e1w_id
import app.tivi.utils.showId
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
class EpisodeWatchEntryTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject lateinit var database: TiviDatabase
    @Inject lateinit var episodesDao: EpisodesDao
    @Inject lateinit var seasonsDao: SeasonsDao
    @Inject lateinit var episodeWatchEntryDao: EpisodeWatchEntryDao

    private val testScope = TestCoroutineScope()

    @Before
    fun setup() {
        DaggerTestComponent.builder()
            .testDataSourceModule(TestDataSourceModule(storeScope = testScope))
            .build()
            .inject(this)

        runBlockingTest {
            // We'll assume that there's a show, season and s1_episodes in the db
            insertShow(database)
            seasonsDao.insert(s1)
            episodesDao.insertAll(s1_episodes)
        }
    }

    @Test
    fun insert() = testScope.runBlockingTest {
        episodeWatchEntryDao.insert(s1e1w)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id), `is`(s1e1w))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() = testScope.runBlockingTest {
        episodeWatchEntryDao.insert(s1e1w)
        // Make a copy with a 0 id
        val copy = s1e1w.copy(id = 0)
        episodeWatchEntryDao.insert(copy)
    }

    @Test
    fun fetchEntries_WithPendingSendAction() = testScope.runBlockingTest {
        episodeWatchEntryDao.insertAll(s1e1w, episodeWatch2PendingSend)
        assertThat(
            episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId),
            `is`(listOf(episodeWatch2PendingSend))
        )
    }

    @Test
    fun fetchEntries_WithPendingDeleteAction() = testScope.runBlockingTest {
        episodeWatchEntryDao.insertAll(s1e1w, episodeWatch2PendingDelete)
        assertThat(
            episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId),
            `is`(listOf(episodeWatch2PendingDelete))
        )
    }

    @Test
    fun delete() = testScope.runBlockingTest {
        episodeWatchEntryDao.insert(s1e1w)
        episodeWatchEntryDao.deleteEntity(s1e1w)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id), `is`(nullValue()))
    }

    @Test
    fun deleteEpisode_deletesWatch() = testScope.runBlockingTest {
        episodeWatchEntryDao.insert(s1e1w)
        // Now delete episode
        episodesDao.deleteEntity(s1e1)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id), `is`(nullValue()))
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
