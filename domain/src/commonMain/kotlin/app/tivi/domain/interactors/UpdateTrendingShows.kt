// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.daos.TrendingDao
import app.tivi.data.shows.ShowStore
import app.tivi.data.trendingshows.TrendingShowsStore
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.domain.UserInitiatedParams
import app.tivi.domain.interactors.UpdateTrendingShows.Params
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.parallelForEach
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateTrendingShows(
  private val trendingShowsStore: Lazy<TrendingShowsStore>,
  private val trendingShowsDao: Lazy<TrendingDao>,
  private val showStore: Lazy<ShowStore>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interactor<Params, Unit>() {
  override suspend fun doWork(params: Params) {
    withContext(dispatchers.io) {
      val page = when {
        params.page >= 0 -> params.page
        params.page == Page.NEXT_PAGE -> {
          val lastPage = trendingShowsDao.value.getLastPage()
          if (lastPage != null) lastPage + 1 else 0
        }

        else -> 0
      }

      trendingShowsStore.value.fetch(page, params.isUserInitiated).parallelForEach {
        showStore.value.fetch(it.showId)
      }
    }
  }

  data class Params(val page: Int, override val isUserInitiated: Boolean = false) : UserInitiatedParams

  data object Page {
    const val NEXT_PAGE = -1
    const val REFRESH = -2
  }
}
