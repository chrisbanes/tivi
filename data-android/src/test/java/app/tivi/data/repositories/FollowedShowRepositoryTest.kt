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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.tivi.data.DaggerTestComponent
import app.tivi.data.TestDataSourceModule
import app.tivi.data.TiviDatabase
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.ErrorResult
import app.tivi.data.entities.Success
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.repositories.followedshows.TraktFollowedShowsDataSource
import app.tivi.utils.SuccessFakeShowDataSource
import app.tivi.utils.SuccessFakeShowImagesDataSource
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
class FollowedShowRepositoryTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testScope = TestCoroutineScope()

    @Inject lateinit var followShowsDao: FollowedShowsDao
    @Inject lateinit var repository: FollowedShowsRepository
    @Inject lateinit var database: TiviDatabase
    @Inject lateinit var traktDataSource: TraktFollowedShowsDataSource

    @Before
    fun setup() {
        DaggerTestComponent.builder()
            .testDataSourceModule(
                TestDataSourceModule(
                    traktShowDataSource = SuccessFakeShowDataSource,
                    tmdbShowDataSource = SuccessFakeShowDataSource,
                    tmdbShowImagesDataSource = SuccessFakeShowImagesDataSource,
                    storeScope = testScope
                )
            )
            .build()
            .inject(this)

        runBlocking {
            // We'll assume that there's a show in the db
            insertShow(database)
        }
    }

    @Test
    fun testSync() = testScope.runBlockingTest {
        coEvery { traktDataSource.getFollowedListId() } returns Success(0)
        coEvery { traktDataSource.getListShows(0) } returns Success(listOf(followedShow1Network to show))

        repository.syncFollowedShows()

        assertThat(
            repository.getFollowedShows(),
            `is`(listOf(followedShow1Local))
        )
    }

    @Test
    fun testSync_emptyResponse() = testScope.runBlockingTest {
        insertFollowedShow(database)

        coEvery { traktDataSource.getFollowedListId() } returns Success(0)
        coEvery { traktDataSource.getListShows(0) } returns Success(emptyList())

        repository.syncFollowedShows()

        assertThat(
            repository.getFollowedShows(),
            `is`(emptyList())
        )
    }

    @Test
    fun testSync_responseDifferentShow() = testScope.runBlockingTest {
        insertFollowedShow(database)

        coEvery { traktDataSource.getFollowedListId() } returns Success(0)
        coEvery { traktDataSource.getListShows(0) } returns Success(listOf(followedShow2Network to show2))

        repository.syncFollowedShows()

        assertThat(
            repository.getFollowedShows(),
            `is`(listOf(followedShow2Local))
        )
    }

    @Test
    fun testSync_pendingDelete() = testScope.runBlockingTest {
        followShowsDao.insert(followedShow1PendingDelete)

        // Return error for the list ID so that we disable syncing
        coEvery { traktDataSource.getFollowedListId() } returns ErrorResult(IllegalArgumentException())

        repository.syncFollowedShows()

        assertThat(
            repository.getFollowedShows(),
            `is`(emptyList())
        )
    }

    @Test
    fun testSync_pendingAdd() = testScope.runBlockingTest {
        followShowsDao.insert(followedShow1PendingUpload)

        // Return an error for the list ID so that we disable syncing
        coEvery { traktDataSource.getFollowedListId() } returns ErrorResult(IllegalArgumentException())

        repository.syncFollowedShows()

        assertThat(
            repository.getFollowedShows(),
            `is`(listOf(followedShow1Local))
        )
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }
}
