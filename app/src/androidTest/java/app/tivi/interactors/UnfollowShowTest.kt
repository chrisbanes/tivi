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

import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.insertShow
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

class UnfollowShowTest : BaseDatabaseTest() {
    private lateinit var unfollowShow: UnfollowShowInteractor

    private lateinit var syncTraktFollowedShowsInteractor: SyncTraktFollowedShowsInteractor

    private lateinit var followShowsDao: FollowedShowsDao
    private lateinit var seasonsDao: SeasonsDao

    override fun setup() {
        super.setup()
        // We'll assume that there's a show and season in the db
        insertShow(db)

        followShowsDao = db.followedShowsDao()
        seasonsDao = db.seasonsDao()

//        runBlocking {
//            val traktFollowedShowsSyncer = mock(TraktFollowedShowsSyncer::class.java)
//            `when`(traktFollowedShowsSyncer.sync()).thenReturn(Unit)
//
//            syncTraktFollowedShowsInteractor = SyncTraktFollowedShowsInteractor(
//                    traktFollowedShowsSyncer,
//                    testCoroutineDispatchers
//            )
//        }
//
//        unfollowShow = UnfollowShowInteractor(
//                testCoroutineDispatchers,
//                seasonsDao,
//                followShowsDao,
//                syncTraktFollowedShowsInteractor
//        )
    }

    @Test
    fun test_doWork() = runBlocking {
//        insertSeason(db)
//        insertEpisodes(db)
//        insertFollowedShow(db)
//
//        unfollowShow(showId)
//
//        // Verify that a sync was started
//        assertThat(followShowsDao.entryWithShowId(showId), `is`(followedShowPendingDelete))
//        assertThat(seasonsDao.seasonWithId(seasonOneId), `is`(nullValue()))
//        verify(syncTraktFollowedShowsInteractor, times(1)).invoke(any())
    }
}