// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.utils

import app.tivi.data.Database
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.SqlDelightEpisodeWatchEntryDao
import app.tivi.data.daos.SqlDelightEpisodesDao
import app.tivi.data.daos.SqlDelightFollowedShowsDao
import app.tivi.data.daos.SqlDelightLastRequestDao
import app.tivi.data.daos.SqlDelightSeasonsDao
import app.tivi.data.daos.SqlDelightTiviShowDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.episodes.EpisodeLastRequestStore
import app.tivi.data.episodes.EpisodeWatchLastRequestStore
import app.tivi.data.episodes.EpisodeWatchStore
import app.tivi.data.episodes.SeasonLastRequestStore
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.episodes.ShowSeasonsLastRequestStore
import app.tivi.data.followedshows.FollowedShowsLastRequestStore
import app.tivi.data.followedshows.FollowedShowsRepository
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

@OptIn(DelicateCoroutinesApi::class)
class ObjectGraph(
  val database: Database,
  val backgroundScope: CoroutineScope = GlobalScope,
  val appCoroutineDispatchers: AppCoroutineDispatchers = createSingleAppCoroutineDispatchers(),
  val tiviShowDao: TiviShowDao = SqlDelightTiviShowDao(database, appCoroutineDispatchers),
  val episodesDao: EpisodesDao = SqlDelightEpisodesDao(database, appCoroutineDispatchers),
  val seasonsDao: SeasonsDao = SqlDelightSeasonsDao(database, appCoroutineDispatchers),
  val episodeWatchEntryDao: EpisodeWatchEntryDao = SqlDelightEpisodeWatchEntryDao(database, appCoroutineDispatchers),
  val followedShowsDao: FollowedShowsDao = SqlDelightFollowedShowsDao(database, appCoroutineDispatchers),
  val followedShowsDataSource: FakeFollowedShowsDataSource = FakeFollowedShowsDataSource(),
  val lastRequestDao: LastRequestDao = SqlDelightLastRequestDao(database),
  val tmdbSeasonsEpisodesDataSource: FakeSeasonsEpisodesDataSource = FakeSeasonsEpisodesDataSource(),
  val traktSeasonsEpisodesDataSource: FakeSeasonsEpisodesDataSource = FakeSeasonsEpisodesDataSource(),
  val tmdbEpisodeDataSource: FakeEpisodeDataSource = FakeEpisodeDataSource(),
  val traktEpisodeDataSource: FakeEpisodeDataSource = FakeEpisodeDataSource(),
  val episodeWatchesDataSource: FakeEpisodeWatchesDataSource = FakeEpisodeWatchesDataSource(),

  val traktAuthRepository: TraktAuthRepository = TraktAuthRepository(
    scope = backgroundScope,
    dispatchers = appCoroutineDispatchers,
    authStore = AuthorizedAuthStore,
    loginAction = lazy { SuccessTraktLoginAction },
    refreshTokenAction = lazy { SuccessRefreshTokenAction },
    logger = FakeLogger,
  ),
  val followedShowsRepository: FollowedShowsRepository = FollowedShowsRepository(
    followedShowsDao = followedShowsDao,
    followedShowsLastRequestStore = FollowedShowsLastRequestStore(lastRequestDao),
    dataSource = followedShowsDataSource,
    traktAuthRepository = traktAuthRepository,
    logger = FakeLogger,
    showDao = tiviShowDao,
    transactionRunner = TestTransactionRunner,
  ),
  val episodeWatchStore: EpisodeWatchStore = EpisodeWatchStore(
    transactionRunner = TestTransactionRunner,
    episodeWatchEntryDao = episodeWatchEntryDao,
    logger = FakeLogger,
  ),
  val seasonsEpisodesRepository: SeasonsEpisodesRepository = SeasonsEpisodesRepository(
    episodeWatchStore = episodeWatchStore,
    episodeWatchLastLastRequestStore = EpisodeWatchLastRequestStore(lastRequestDao),
    episodeLastRequestStore = EpisodeLastRequestStore(lastRequestDao),
    seasonLastRequestStore = SeasonLastRequestStore(lastRequestDao),
    transactionRunner = TestTransactionRunner,
    seasonsDao = seasonsDao,
    episodesDao = episodesDao,
    showDao = tiviShowDao,
    showSeasonsLastRequestStore = ShowSeasonsLastRequestStore(lastRequestDao),
    tmdbSeasonsDataSource = tmdbSeasonsEpisodesDataSource,
    traktSeasonsDataSource = traktSeasonsEpisodesDataSource,
    traktEpisodeDataSource = traktEpisodeDataSource,
    tmdbEpisodeDataSource = tmdbEpisodeDataSource,
    traktEpisodeWatchesDataSource = episodeWatchesDataSource,
    traktAuthRepository = traktAuthRepository,
    logger = FakeLogger,
  ),
)
