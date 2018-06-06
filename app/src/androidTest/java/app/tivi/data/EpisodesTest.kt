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

import android.database.sqlite.SQLiteConstraintException
import android.support.test.runner.AndroidJUnit4
import app.tivi.data.BaseTest
import app.tivi.data.SampleData
import app.tivi.data.daos.EpisodesDao
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

class EpisodesTest : BaseTest() {
    private lateinit var episodeDao: EpisodesDao

    override fun setup() {
        super.setup()
        episodeDao = db.episodesDao()
        // We'll assume that there's a show and season in the db
        SampleData.insertShow(db)
        SampleData.insertSeason(db)
    }

    @Test
    fun insert() {
        episodeDao.insert(SampleData.episodeOne)
        assertThat(episodeDao.episodeWithId(SampleData.episodeOne.id!!), `is`(SampleData.episodeOne))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() {
        episodeDao.insert(SampleData.episodeOne)
        // Make a copy with a null id
        val copy = SampleData.episodeOne.copy(id = null)
        episodeDao.insert(copy)
    }

    @Test
    fun delete() {
        episodeDao.insert(SampleData.episodeOne)
        episodeDao.delete(SampleData.episodeOne)
        assertThat(episodeDao.episodeWithId(SampleData.episodeOne.id!!), `is`(nullValue()))
    }

    @Test
    fun deleteSeason_deletesEpisode() {
        episodeDao.insert(SampleData.episodeOne)
        // Now delete season
        SampleData.deleteSeason(db)
        assertThat(episodeDao.episodeWithId(SampleData.episodeOne.id!!), `is`(nullValue()))
    }
}