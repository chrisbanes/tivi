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

package app.tivi.interactors

import app.tivi.SeasonFetcher
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.insertShow
import app.tivi.utils.showId
import app.tivi.utils.testCoroutineDispatchers
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

class FollowInteractorTest : BaseDatabaseTest() {
    private lateinit var followShowCall: FollowShowInteractor
    private lateinit var followShowsDao: FollowedShowsDao
    private lateinit var seasonFetcher: SeasonFetcher

    override fun setup() {
        super.setup()
        // We'll assume that there's a show and season in the db
        insertShow(db)

        followShowsDao = db.followedShowsDao()
        seasonFetcher = mock(SeasonFetcher::class.java)
        followShowCall = FollowShowInteractor(testCoroutineDispatchers, followShowsDao, seasonFetcher)
    }

    @Test
    fun test_doWork() {
        runBlocking {
            followShowCall.doWork(showId)

            assertThat(followShowsDao.entryWithShowId(showId), `is`(notNullValue()))
            verify(seasonFetcher, times(1)).load(showId)
        }
    }
}