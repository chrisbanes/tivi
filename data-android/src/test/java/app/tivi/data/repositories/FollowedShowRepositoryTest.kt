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

package app.tivi.data.repositories

import app.tivi.data.TiviEntityInserter
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.Success
import app.tivi.data.repositories.followedshows.FollowedShowsDataSource
import app.tivi.data.repositories.followedshows.FollowedShowsLastRequestStore
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.repositories.followedshows.FollowedShowsStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.TestTransactionRunner
import app.tivi.utils.followedShow1Local
import app.tivi.utils.followedShow1Network
import app.tivi.utils.followedShow1PendingDelete
import app.tivi.utils.followedShow1PendingUpload
import app.tivi.utils.followedShow2Local
import app.tivi.utils.followedShow2Network
import app.tivi.utils.insertFollowedShow
import app.tivi.utils.insertShow
import app.tivi.utils.show
import app.tivi.utils.show2
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import javax.inject.Provider

class FollowedShowRepositoryTest : BaseDatabaseTest() {
    private lateinit var followShowsDao: FollowedShowsDao

    private lateinit var traktDataSource: FollowedShowsDataSource
    private lateinit var showRepository: ShowRepository

    private lateinit var repository: FollowedShowsRepository

    override fun setup() {
        super.setup()

        runBlockingTest {
            // We'll assume that there's a show in the db
            insertShow(db)

            followShowsDao = db.followedShowsDao()

            showRepository = mockk(relaxUnitFun = true)
            coEvery { showRepository.needsUpdate(any()) } returns true
            coEvery { showRepository.needsInitialUpdate(any()) } returns true
            coEvery { showRepository.needsImagesUpdate(any(), any()) } returns true

            val logger = mockk<Logger>(relaxUnitFun = true)
            val txRunner = TestTransactionRunner
            val entityInserter = TiviEntityInserter(txRunner, logger)
            traktDataSource = mockk()

            repository = FollowedShowsRepository(
                    FollowedShowsStore(txRunner, entityInserter, db.followedShowsDao(), logger),
                    FollowedShowsLastRequestStore(db.lastRequestDao()),
                    ShowStore(entityInserter, db.showDao(), db.showFtsDao(), db.showImagesDao(), txRunner),
                    traktDataSource,
                    showRepository,
                    Provider { TraktAuthState.LOGGED_IN },
                    logger
            )
        }
    }

    @Test
    fun testSync() = runBlockingTest {
        coEvery { traktDataSource.getFollowedListId() } returns 0
        coEvery { traktDataSource.getListShows(0) } returns Success(listOf(followedShow1Network to show))

        repository.syncFollowedShows()

        assertThat(repository.getFollowedShows(), `is`(listOf(followedShow1Local)))
    }

    @Test
    fun testSync_emptyResponse() = runBlockingTest {
        insertFollowedShow(db)

        coEvery { traktDataSource.getFollowedListId() } returns 0
        coEvery { traktDataSource.getListShows(0) } returns Success(emptyList())

        repository.syncFollowedShows()

        assertThat(repository.getFollowedShows(), `is`(emptyList()))
    }

    @Test
    fun testSync_responseDifferentShow() = runBlockingTest {
        insertFollowedShow(db)

        coEvery { traktDataSource.getFollowedListId() } returns 0
        coEvery { traktDataSource.getListShows(0) } returns Success(listOf(followedShow2Network to show2))

        repository.syncFollowedShows()

        assertThat(repository.getFollowedShows(), `is`(listOf(followedShow2Local)))
    }

    @Test
    fun testSync_pendingDelete() = runBlockingTest {
        followShowsDao.insert(followedShow1PendingDelete)

        // Return null for the list ID so that we disable syncing
        coEvery { traktDataSource.getFollowedListId() } returns null

        repository.syncFollowedShows()

        assertThat(repository.getFollowedShows(), `is`(emptyList()))
    }

    @Test
    fun testSync_pendingAdd() = runBlockingTest {
        followShowsDao.insert(followedShow1PendingUpload)

        // Return null for the list ID so that we disable syncing
        coEvery { traktDataSource.getFollowedListId() } returns null

        repository.syncFollowedShows()

        assertThat(repository.getFollowedShows(), `is`(listOf(followedShow1Local)))
    }
}