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
import app.tivi.utils.deleteSeason
import app.tivi.utils.episodeOne
import app.tivi.utils.insertSeason
import app.tivi.utils.insertShow
import app.tivi.utils.showId
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test

class EpisodesTest : BaseDatabaseTest() {
    private lateinit var episodeDao: EpisodesDao

    override fun setup() {
        super.setup()
        episodeDao = db.episodesDao()
        // We'll assume that there's a show and season in the db
        insertShow(db)
        insertSeason(db)
    }

    @Test
    fun insert() {
        episodeDao.insert(episodeOne)
        assertThat(episodeDao.episodeWithId(episodeOne.id), `is`(episodeOne))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() {
        episodeDao.insert(episodeOne)
        // Make a copy with a 0 id
        val copy = episodeOne.copy(id = 0)
        episodeDao.insert(copy)
    }

    @Test
    fun delete() {
        episodeDao.insert(episodeOne)
        episodeDao.delete(episodeOne)
        assertThat(episodeDao.episodeWithId(episodeOne.id), `is`(nullValue()))
    }

    @Test
    fun deleteSeason_deletesEpisode() {
        episodeDao.insert(episodeOne)
        // Now delete season
        deleteSeason(db)
        assertThat(episodeDao.episodeWithId(episodeOne.id), `is`(nullValue()))
    }

    @Test
    fun showIdForEpisodeId() {
        episodeDao.insert(episodeOne)
        assertThat(episodeDao.showIdForEpisodeId(episodeOne.id), `is`(showId))
    }
}