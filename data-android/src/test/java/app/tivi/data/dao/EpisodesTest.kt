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
import app.tivi.data.daos.EpisodesDao
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.insertShow
import app.tivi.utils.runBlockingTest
import app.tivi.utils.s1
import app.tivi.utils.s1e1
import app.tivi.utils.showId
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Ignore
import org.junit.Test

class EpisodesTest : BaseDatabaseTest() {
    private lateinit var episodeDao: EpisodesDao

    override fun setup() {
        super.setup()

        runBlocking {
            episodeDao = db.episodesDao()
            // We'll assume that there's a show and season in the db
            insertShow(db)
            db.seasonsDao().insert(s1)
        }
    }

    @Test
    fun insert() = runBlockingTest {
        episodeDao.insert(s1e1)
        assertThat(episodeDao.episodeWithId(s1e1.id), `is`(s1e1))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() = runBlockingTest {
        episodeDao.insert(s1e1)
        // Make a copy with a 0 id
        val copy = s1e1.copy(id = 0)
        episodeDao.insert(copy)
    }

    @Test
    fun delete() = runBlockingTest {
        episodeDao.insert(s1e1)
        episodeDao.delete(s1e1)
        assertThat(episodeDao.episodeWithId(s1e1.id), `is`(nullValue()))
    }

    @Test
    fun deleteSeason_deletesEpisode() = runBlockingTest {
        episodeDao.insert(s1e1)
        // Now delete season
        db.seasonsDao().delete(s1)
        assertThat(episodeDao.episodeWithId(s1e1.id), `is`(nullValue()))
    }

    @Test
    fun showIdForEpisodeId() = runBlockingTest {
        episodeDao.insert(s1e1)
        assertThat(episodeDao.showIdForEpisodeId(s1e1.id), `is`(showId))
    }
}