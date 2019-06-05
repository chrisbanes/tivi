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

import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.Success
import app.tivi.data.repositories.followedshows.FollowedShowsDataSource
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.repositories.followedshows.FollowedShowsLastRequestStore
import app.tivi.data.repositories.followedshows.FollowedShowsStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.TestTransactionRunner
import app.tivi.utils.followedShow1
import app.tivi.utils.followedShow1PendingDelete
import app.tivi.utils.followedShow1PendingUpload
import app.tivi.utils.followedShow2
import app.tivi.utils.insertFollowedShow
import app.tivi.utils.insertShow
import app.tivi.utils.runBlockingTest
import app.tivi.utils.show
import app.tivi.utils.show2
import app.tivi.utils.show2Id
import app.tivi.utils.showId
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Ignore
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import javax.inject.Provider

@Ignore("https://github.com/robolectric/robolectric/issues/3556")
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

            showRepository = mock(ShowRepository::class.java)
            `when`(showRepository.needsUpdate(any(Long::class.java))).thenReturn(true)

            val logger = mock(Logger::class.java)
            val txRunner = TestTransactionRunner
            val entityInserter = EntityInserter(txRunner, logger)
            traktDataSource = mock(FollowedShowsDataSource::class.java)

            repository = FollowedShowsRepository(
                    FollowedShowsStore(txRunner, entityInserter, db.followedShowsDao(), db.showDao(), logger),
                    FollowedShowsLastRequestStore(db.lastRequestDao()),
                    ShowStore(entityInserter, db.showDao(), txRunner),
                    traktDataSource,
                    showRepository,
                    Provider { TraktAuthState.LOGGED_IN },
                    logger
            )
        }
    }

    @Test
    fun testSync() = runBlockingTest {
        `when`(traktDataSource.getFollowedListId()).thenReturn(0)
        `when`(traktDataSource.getListShows(0)).thenReturn(Success(listOf(followedShow1 to show)))

        assertThat(repository.getFollowedShows(), `is`(listOf(followedShow1)))

        // Verify that a show update was triggered
        verify(showRepository, times(1)).updateShow(showId)
    }

    @Test
    fun testSync_emptyResponse() = runBlockingTest {
        insertFollowedShow(db)

        `when`(traktDataSource.getFollowedListId()).thenReturn(0)
        `when`(traktDataSource.getListShows(0)).thenReturn(Success(emptyList()))

        assertThat(repository.getFollowedShows(), `is`(emptyList()))
    }

    @Test
    fun testSync_responseDifferentShow() = runBlockingTest {
        insertFollowedShow(db)

        `when`(traktDataSource.getFollowedListId()).thenReturn(0)
        `when`(traktDataSource.getListShows(0)).thenReturn(Success(listOf(followedShow2 to show2)))

        assertThat(repository.getFollowedShows(), `is`(listOf(followedShow2)))

        // Verify that a show update was triggered
        verify(showRepository, times(1)).updateShow(show2Id)
    }

    @Test
    fun testSync_pendingDelete() = runBlockingTest {
        followShowsDao.insert(followedShow1PendingDelete)

        // Return null for the list ID so that we disable syncing
        `when`(traktDataSource.getFollowedListId()).thenReturn(null)

        assertThat(repository.getFollowedShows(), `is`(emptyList()))
    }

    @Test
    fun testSync_pendingAdd() = runBlockingTest {
        followShowsDao.insert(followedShow1PendingUpload)

        // Return null for the list ID so that we disable syncing
        `when`(traktDataSource.getFollowedListId()).thenReturn(null)

        assertThat(repository.getFollowedShows(), `is`(listOf(followedShow1)))
    }
}