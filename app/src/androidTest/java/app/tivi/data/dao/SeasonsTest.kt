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

package app.tivi.data.dao

import android.database.sqlite.SQLiteConstraintException
import app.tivi.data.daos.SeasonsDao
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.deleteShow
import app.tivi.utils.insertShow
import app.tivi.utils.seasonOne
import app.tivi.utils.seasonOneId
import app.tivi.utils.seasonSpecials
import app.tivi.utils.seasonTwo
import app.tivi.utils.showId
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test

class SeasonsTest : BaseDatabaseTest() {
    private lateinit var seasonsDao: SeasonsDao

    override fun setup() {
        super.setup()
        seasonsDao = db.seasonsDao()
        // We'll assume that there's a show in the db
        insertShow(db)
    }

    @Test
    fun insertSeason() {
        seasonsDao.insert(seasonOne)

        assertThat(seasonsDao.seasonWithId(seasonOneId), `is`(seasonOne))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() {
        seasonsDao.insert(seasonOne)

        // Make a copy with a null id
        val copy = seasonOne.copy(id = null)

        seasonsDao.insert(copy)
    }

    @Test
    fun specialsOrder() {
        seasonsDao.insert(seasonSpecials)
        seasonsDao.insert(seasonOne)
        seasonsDao.insert(seasonTwo)

        // Specials should always be last
        assertThat(seasonsDao.seasonsForShowId(showId),
                `is`(listOf(seasonOne, seasonTwo, seasonSpecials))
        )
    }

    @Test
    fun deleteSeason() {
        seasonsDao.insert(seasonOne)
        seasonsDao.delete(seasonOne)

        assertThat(seasonsDao.seasonWithId(seasonOneId), `is`(nullValue()))
    }

    @Test
    fun deleteShow_deletesSeason() {
        seasonsDao.insert(seasonOne)
        // Now delete show
        deleteShow(db)

        assertThat(seasonsDao.seasonWithId(seasonOneId), `is`(nullValue()))
    }
}