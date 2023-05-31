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
import app.tivi.utils.FakeFollowedShowsDataSource
import app.tivi.utils.followedShow1Local
import app.tivi.utils.followedShow1Network
import app.tivi.utils.followedShow1PendingDelete
import app.tivi.utils.followedShow1PendingUpload
import app.tivi.utils.followedShow2Local
import app.tivi.utils.followedShow2Network
import app.tivi.utils.show
import app.tivi.utils.show2
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import me.tatarka.inject.annotations.Component

class FollowedShowRepositoryTest : DatabaseTest() {
    private lateinit var showsDao: TiviShowDao
    private lateinit var followShowsDao: FollowedShowsDao
    private lateinit var followedShowsRepository: FollowedShowsRepository
    private lateinit var followedShowsDataSource: FakeFollowedShowsDataSource
    private lateinit var traktAuthRepository: TraktAuthRepository

    @BeforeTest
    fun setup() {
        val component = FollowedShowsRepositoryTestComponent::class.create()
        showsDao = component.showsDao
        followShowsDao = component.followShowsDao
        followedShowsRepository = component.followedShowsRepository
        followedShowsDataSource = component.followedShowsDataSource as FakeFollowedShowsDataSource
        traktAuthRepository = component.traktAuthRepository

        // We'll assume that there's a show in the db
        showsDao.insert(show)
        showsDao.insert(show2)
    }

    @Test
    fun testSync() = runTest {
        followedShowsDataSource.getFollowedListIdResult =
            Result.success(TraktList(ids = TraktListIds(trakt = 0)))
        followedShowsDataSource.getListShowsResult =
            Result.success(listOf(followedShow1Network to show))

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows())
            .containsExactly(followedShow1Local)
    }

    @Test
    fun testSync_emptyResponse() = runTest {
        followShowsDao.insert(followedShow1Local)

        followedShowsDataSource.getFollowedListIdResult =
            Result.success(TraktList(ids = TraktListIds(trakt = 0)))
        followedShowsDataSource.getListShowsResult = Result.success(emptyList())

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows()).isEmpty()
    }

    @Test
    fun testSync_responseDifferentShow() = runTest {
        followShowsDao.insert(followedShow1Local)

        followedShowsDataSource.getFollowedListIdResult =
            Result.success(TraktList(ids = TraktListIds(trakt = 0)))
        followedShowsDataSource.getListShowsResult =
            Result.success(listOf(followedShow2Network to show2))

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows())
            .containsExactly(followedShow2Local)
    }

    @Test
    fun testSync_pendingDelete() = runTest {
        followShowsDao.insert(followedShow1PendingDelete)

        // Return error for the list ID so that we disable syncing
        followedShowsDataSource.getFollowedListIdResult =
            Result.failure(IllegalArgumentException())

        traktAuthRepository.onNewAuthState(AuthorizedAuthState)

        followedShowsRepository.syncFollowedShows()

        assertThat(followedShowsRepository.getFollowedShows()).isEmpty()
    }

    @Test
    fun testSync_pendingAdd() = runTest {
        followShowsDao.insert(followedShow1PendingUpload)

        // Return an error for the list ID so that we disable syncing
        followedShowsDataSource.getFollowedListIdResult = Result.failure(IllegalArgumentException())

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
