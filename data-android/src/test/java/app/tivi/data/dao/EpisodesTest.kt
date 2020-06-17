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
import app.tivi.data.DatabaseModuleBinds
import app.tivi.data.DatabaseTest
import app.tivi.data.TiviDatabase
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.utils.insertShow
import app.tivi.utils.s1
import app.tivi.utils.s1_episodes
import app.tivi.utils.s1e1
import app.tivi.utils.s1e2
import app.tivi.utils.s1e3
import app.tivi.utils.showId
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

@UninstallModules(DatabaseModuleBinds::class)
@HiltAndroidTest
class EpisodesTest : DatabaseTest() {
    @Inject lateinit var database: TiviDatabase
    @Inject lateinit var episodeDao: EpisodesDao
    @Inject lateinit var seasonsDao: SeasonsDao

    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            // We'll assume that there's a show and season in the db
            insertShow(database)
            seasonsDao.insert(s1)
        }
    }

    @Test
    fun insert() = testScope.runBlockingTest {
        episodeDao.insert(s1e1)
        assertThat(episodeDao.episodeWithId(s1e1.id), `is`(s1e1))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() = testScope.runBlockingTest {
        episodeDao.insert(s1e1)
        // Make a copy with a 0 id
        val copy = s1e1.copy(id = 0)
        episodeDao.insert(copy)
    }

    @Test
    fun delete() = testScope.runBlockingTest {
        episodeDao.insert(s1e1)
        episodeDao.deleteEntity(s1e1)
        assertThat(episodeDao.episodeWithId(s1e1.id), `is`(nullValue()))
    }

    @Test
    fun deleteSeason_deletesEpisode() = testScope.runBlockingTest {
        episodeDao.insert(s1e1)
        // Now delete season
        seasonsDao.deleteEntity(s1)
        assertThat(episodeDao.episodeWithId(s1e1.id), `is`(nullValue()))
    }

    @Test
    fun showIdForEpisodeId() = testScope.runBlockingTest {
        episodeDao.insert(s1e1)
        assertThat(episodeDao.showIdForEpisodeId(s1e1.id), `is`(showId))
    }

    @Test
    fun nextAiredEpisodeAfter() = testScope.runBlockingTest {
        episodeDao.insertAll(s1_episodes)

        assertThat(
            episodeDao.observeNextEpisodeForShowAfter(showId, 0, 0)
                .first()?.episode,
            `is`(s1e1)
        )

        assertThat(
            episodeDao.observeNextEpisodeForShowAfter(showId, 1, 0)
                .first()?.episode,
            `is`(s1e2)
        )

        assertThat(
            episodeDao.observeNextEpisodeForShowAfter(showId, 1, 1)
                .first()?.episode,
            `is`(s1e3)
        )

        assertThat(
            episodeDao.observeNextEpisodeForShowAfter(showId, 1, 2)
                .first()?.episode,
            nullValue()
        )
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
