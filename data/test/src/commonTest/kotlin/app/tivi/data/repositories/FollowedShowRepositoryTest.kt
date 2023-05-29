// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.repositories

import app.cash.sqldelight.db.SqlDriver
import app.moviebase.trakt.model.TraktList
import app.moviebase.trakt.model.TraktListIds
import app.tivi.data.DatabaseTest
import app.tivi.data.TestApplicationComponent
import app.tivi.data.create
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.followedshows.FollowedShowsDataSource
import app.tivi.data.followedshows.FollowedShowsRepository
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.utils.AuthorizedAuthState
import app.tivi.utils.followedShow1Local
import app.tivi.utils.followedShow1Network
import app.tivi.utils.followedShow1PendingDelete
import app.tivi.utils.followedShow1PendingUpload
import app.tivi.utils.followedShow2Local
import app.tivi.utils.followedShow2Network
import app.tivi.utils.show
import app.tivi.utils.show2
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import me.tatarka.inject.annotations.Component
import org.junit.Before
import org.junit.Test

class FollowedShowRepositoryTest : DatabaseTest() {
    private lateinit var showsDao: TiviShowDao
    private lateinit var followShowsDao: FollowedShowsDao
    private lateinit var followedShowsRepository: FollowedShowsRepository
    private lateinit var followedShowsDataSource: FollowedShowsDataSource
    private lateinit var traktAuthRepository: TraktAuthRepository

    @Before
    fun setup() {
        val component = FollowedShowsRepositoryTestComponent::class.create()
        showsDao = component.showsDao
        followShowsDao = component.followShowsDao
        followedShowsRepository = component.followedShowsRepository
        followedShowsDataSource = component.followedShowsDataSource
        traktAuthRepository = component.traktAuthRepository

        // We'll assume that there's a show in the db
        showsDao.insert(show)
        showsDao.insert(show2)
    }

    @Test
    fun testSync() = runTest {
        coEvery { followedShowsDataSource.getFollowedListId() }
            .returns(TraktList(ids = TraktListIds(trakt = 0)))
        coEvery { followedShowsDataSource.getListShows(0) }
            .returns(listOf(followedShow1Network to show))

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows())
            .containsExactly(followedShow1Local)
    }

    @Test
    fun testSync_emptyResponse() = runTest {
        followShowsDao.insert(followedShow1Local)

        coEvery { followedShowsDataSource.getFollowedListId() } returns
            TraktList(ids = TraktListIds(trakt = 0))

        coEvery { followedShowsDataSource.getListShows(0) } returns emptyList()

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows()).isEmpty()
    }

    @Test
    fun testSync_responseDifferentShow() = runTest {
        followShowsDao.insert(followedShow1Local)

        coEvery { followedShowsDataSource.getFollowedListId() } returns
            TraktList(ids = TraktListIds(trakt = 0))
        coEvery { followedShowsDataSource.getListShows(0) } returns listOf(followedShow2Network to show2)

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows())
            .containsExactly(followedShow2Local)
    }

    @Test
    fun testSync_pendingDelete() = runTest {
        followShowsDao.insert(followedShow1PendingDelete)

        // Return error for the list ID so that we disable syncing
        coEvery { followedShowsDataSource.getFollowedListId() } throws IllegalArgumentException()

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows()).isEmpty()
    }

    @Test
    fun testSync_pendingAdd() = runTest {
        followShowsDao.insert(followedShow1PendingUpload)

        // Return an error for the list ID so that we disable syncing
        coEvery { followedShowsDataSource.getFollowedListId() } throws IllegalArgumentException()

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows())
            .containsExactly(followedShow1Local)
    }
}

@Component
abstract class FollowedShowsRepositoryTestComponent(
    @Component val testApplicationComponent: TestApplicationComponent =
        TestApplicationComponent::class.create(),
) {
    abstract val showsDao: TiviShowDao
    abstract val followShowsDao: FollowedShowsDao
    abstract val followedShowsRepository: FollowedShowsRepository
    abstract val followedShowsDataSource: FollowedShowsDataSource
    abstract val traktAuthRepository: TraktAuthRepository

    abstract val sqlDriver: SqlDriver
}
