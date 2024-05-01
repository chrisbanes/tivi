// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.repositories

import app.moviebase.trakt.model.TraktList
import app.moviebase.trakt.model.TraktListIds
import app.tivi.data.DatabaseTest
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.utils.FakeFollowedShowsDataSource
import app.tivi.utils.ObjectGraph
import app.tivi.utils.createSingleAppCoroutineDispatchers
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class FollowedShowRepositoryTest : DatabaseTest() {

  private val testScope = TestScope()

  private val objectGraph by lazy {
    ObjectGraph(
      database = database,
      backgroundScope = testScope.backgroundScope,
      appCoroutineDispatchers = createSingleAppCoroutineDispatchers(StandardTestDispatcher(testScope.testScheduler)),
    )
  }

  private val showsDao: TiviShowDao get() = objectGraph.tiviShowDao
  private val followShowsDao: FollowedShowsDao get() = objectGraph.followedShowsDao
  private val followedShowsDataSource: FakeFollowedShowsDataSource get() = objectGraph.followedShowsDataSource
  private val traktAuthRepository get() = objectGraph.traktAuthRepository
  private val followedShowsRepository get() = objectGraph.followedShowsRepository

  @BeforeTest
  fun setup() {
    // We'll assume that there's a show in the db
    showsDao.insert(show)
    showsDao.insert(show2)
  }

  @Test
  fun testSync() = testScope.runTest {
    followedShowsDataSource.getFollowedListIdResult =
      Result.success(TraktList(ids = TraktListIds(trakt = 0)))
    followedShowsDataSource.getListShowsResult =
      Result.success(listOf(followedShow1Network to show))

    traktAuthRepository.login()

    followedShowsRepository.syncFollowedShows()

    assertThat(followedShowsRepository.getFollowedShows())
      .containsExactly(followedShow1Local)
  }

  @Test
  fun testSync_emptyResponse() = testScope.runTest {
    followShowsDao.insert(followedShow1Local)

    followedShowsDataSource.getFollowedListIdResult =
      Result.success(TraktList(ids = TraktListIds(trakt = 0)))
    followedShowsDataSource.getListShowsResult = Result.success(emptyList())

    traktAuthRepository.login()

    followedShowsRepository.syncFollowedShows()

    assertThat(followedShowsRepository.getFollowedShows()).isEmpty()
  }

  @Test
  fun testSync_responseDifferentShow() = testScope.runTest {
    followShowsDao.insert(followedShow1Local)

    followedShowsDataSource.getFollowedListIdResult =
      Result.success(TraktList(ids = TraktListIds(trakt = 0)))
    followedShowsDataSource.getListShowsResult =
      Result.success(listOf(followedShow2Network to show2))

    traktAuthRepository.login()

    followedShowsRepository.syncFollowedShows()

    assertThat(followedShowsRepository.getFollowedShows())
      .containsExactly(followedShow2Local)
  }

  @Test
  fun testSync_pendingDelete() = testScope.runTest {
    followShowsDao.insert(followedShow1PendingDelete)

    // Return error for the list ID so that we disable syncing
    followedShowsDataSource.getFollowedListIdResult =
      Result.failure(IllegalArgumentException())

    traktAuthRepository.login()

    followedShowsRepository.syncFollowedShows()

    assertThat(followedShowsRepository.getFollowedShows()).isEmpty()
  }

  @Test
  fun testSync_pendingAdd() = testScope.runTest {
    followShowsDao.insert(followedShow1PendingUpload)

    // Return an error for the list ID so that we disable syncing
    followedShowsDataSource.getFollowedListIdResult = Result.failure(IllegalArgumentException())

    traktAuthRepository.login()

    followedShowsRepository.syncFollowedShows()

    assertThat(followedShowsRepository.getFollowedShows())
      .containsExactly(followedShow1Local)
  }
}
