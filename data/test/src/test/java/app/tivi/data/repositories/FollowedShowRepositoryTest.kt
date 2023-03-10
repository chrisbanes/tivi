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

import androidx.test.core.app.ApplicationProvider
import app.moviebase.trakt.model.TraktList
import app.moviebase.trakt.model.TraktListIds
import app.tivi.data.DatabaseTest
import app.tivi.data.TestApplicationComponent
import app.tivi.data.TiviRoomDatabase
import app.tivi.data.create
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.followedshows.FollowedShowsDataSource
import app.tivi.data.followedshows.FollowedShowsRepository
import app.tivi.trakt.TraktManager
import app.tivi.utils.AuthorizedAuthState
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
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.tatarka.inject.annotations.Component
import org.junit.After
import org.junit.Before
import org.junit.Test

class FollowedShowRepositoryTest : DatabaseTest() {
    private lateinit var followShowsDao: FollowedShowsDao
    private lateinit var followedShowsRepository: FollowedShowsRepository
    private lateinit var followedShowsDataSource: FollowedShowsDataSource
    private lateinit var database: TiviRoomDatabase
    private lateinit var traktManager: TraktManager

    @Before
    fun setup() {
        val component = FollowedShowsRepositoryTestComponent::class.create()
        followShowsDao = component.followShowsDao
        followedShowsRepository = component.followedShowsRepository
        database = component.database
        followedShowsDataSource = component.followedShowsDataSource
        traktManager = component.traktManager

        runBlocking {
            // We'll assume that there's a show in the db
            insertShow(database)
        }
    }

    @Test
    fun testSync() = runTest {
        coEvery { followedShowsDataSource.getFollowedListId() }
            .returns(TraktList(ids = TraktListIds(trakt = 0)))
        coEvery { followedShowsDataSource.getListShows(0) }
            .returns(listOf(followedShow1Network to show))

        traktManager.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows())
            .containsExactly(followedShow1Local)
    }

    @Test
    fun testSync_emptyResponse() = runTest {
        insertFollowedShow(database)

        coEvery { followedShowsDataSource.getFollowedListId() } returns
            TraktList(ids = TraktListIds(trakt = 0))

        coEvery { followedShowsDataSource.getListShows(0) } returns emptyList()

        traktManager.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows()).isEmpty()
    }

    @Test
    fun testSync_responseDifferentShow() = runTest {
        insertFollowedShow(database)

        coEvery { followedShowsDataSource.getFollowedListId() } returns
            TraktList(ids = TraktListIds(trakt = 0))
        coEvery { followedShowsDataSource.getListShows(0) } returns listOf(followedShow2Network to show2)

        traktManager.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows())
            .containsExactly(followedShow2Local)
    }

    @Test
    fun testSync_pendingDelete() = runTest {
        followShowsDao.upsert(followedShow1PendingDelete)

        // Return error for the list ID so that we disable syncing
        coEvery { followedShowsDataSource.getFollowedListId() } throws IllegalArgumentException()

        traktManager.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows()).isEmpty()
    }

    @Test
    fun testSync_pendingAdd() = runTest {
        followShowsDao.upsert(followedShow1PendingUpload)

        // Return an error for the list ID so that we disable syncing
        coEvery { followedShowsDataSource.getFollowedListId() } throws IllegalArgumentException()

        traktManager.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows())
            .containsExactly(followedShow1Local)
    }

    @After
    fun after() {
        database.close()
    }
}

@Component
abstract class FollowedShowsRepositoryTestComponent(
    @Component val testApplicationComponent: TestApplicationComponent =
        TestApplicationComponent::class.create(ApplicationProvider.getApplicationContext()),
) {
    abstract val followShowsDao: FollowedShowsDao
    abstract val followedShowsRepository: FollowedShowsRepository
    abstract val followedShowsDataSource: FollowedShowsDataSource
    abstract val database: TiviRoomDatabase
    abstract val traktManager: TraktManager
}
