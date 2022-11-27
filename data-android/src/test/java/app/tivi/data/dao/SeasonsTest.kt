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
import app.tivi.data.daos.SeasonsDao
import app.tivi.utils.deleteShow
import app.tivi.utils.insertShow
import app.tivi.utils.s0
import app.tivi.utils.s1
import app.tivi.utils.s1_id
import app.tivi.utils.s2
import app.tivi.utils.showId
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

@UninstallModules(DatabaseModuleBinds::class)
@HiltAndroidTest
class SeasonsTest : DatabaseTest() {
    @Inject lateinit var database: TiviDatabase

    @Inject lateinit var seasonsDao: SeasonsDao

    @Before
    fun setup() {
        hiltRule.inject()

        runBlocking {
            // We'll assume that there's a show in the db
            insertShow(database)
        }
    }

    @Test
    fun insertSeason() = runTest {
        seasonsDao.insert(s1)

        assertThat(seasonsDao.seasonWithId(s1_id), `is`(s1))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() = runTest {
        seasonsDao.insert(s1)

        // Make a copy with a 0 id
        val copy = s1.copy(id = 0)

        seasonsDao.insert(copy)
    }

    @Test
    fun specialsOrder() = runTest {
        seasonsDao.insert(s0)
        seasonsDao.insert(s1)
        seasonsDao.insert(s2)

        // Specials should always be last
        assertThat(
            seasonsDao.seasonsForShowId(showId),
            `is`(listOf(s1, s2, s0))
        )
    }

    @Test
    fun deleteSeason() = runTest {
        seasonsDao.insert(s1)
        seasonsDao.deleteEntity(s1)

        assertThat(seasonsDao.seasonWithId(s1_id), `is`(nullValue()))
    }

    @Test
    fun deleteShow_deletesSeason() = runTest {
        seasonsDao.insert(s1)
        // Now delete show
        deleteShow(database)

        assertThat(seasonsDao.seasonWithId(s1_id), `is`(nullValue()))
    }
}
