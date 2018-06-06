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

package app.tivi.data

import android.database.sqlite.SQLiteConstraintException
import app.tivi.data.daos.SeasonsDao
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test

class SeasonsTest : BaseTest() {
    private lateinit var seasonsDao: SeasonsDao

    override fun setup() {
        super.setup()
        seasonsDao = db.seasonsDao()
        // We'll assume that there's a show in the db
        SampleData.insertShow(db)
    }

    @Test
    fun insertSeason() {
        seasonsDao.insert(SampleData.seasonOne)

        assertThat(seasonsDao.seasonWithId(SampleData.seasonOneId), `is`(SampleData.seasonOne))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() {
        seasonsDao.insert(SampleData.seasonOne)

        // Make a copy with a null id
        val copy = SampleData.seasonOne.copy(id = null)

        seasonsDao.insert(copy)
    }

    @Test
    fun specialsOrder() {
        seasonsDao.insert(SampleData.seasonSpecials)
        seasonsDao.insert(SampleData.seasonOne)
        seasonsDao.insert(SampleData.seasonTwo)

        // Specials should always be last
        assertThat(seasonsDao.seasonsForShowId(SampleData.showId),
                `is`(listOf(SampleData.seasonOne, SampleData.seasonTwo, SampleData.seasonSpecials))
        )
    }

    @Test
    fun deleteSeason() {
        seasonsDao.insert(SampleData.seasonOne)
        seasonsDao.delete(SampleData.seasonOne)

        assertThat(seasonsDao.seasonWithId(SampleData.seasonOneId), `is`(nullValue()))
    }

    @Test
    fun deleteShow_deletesSeason() {
        seasonsDao.insert(SampleData.seasonOne)
        // Now delete show
        SampleData.deleteShow(db)

        assertThat(seasonsDao.seasonWithId(SampleData.seasonOneId), `is`(nullValue()))
    }
}