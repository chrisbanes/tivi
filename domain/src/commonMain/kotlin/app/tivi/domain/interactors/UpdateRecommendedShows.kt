// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.recommendedshows.RecommendedShowsStore
import app.tivi.data.shows.ShowStore
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.domain.UserInitiatedParams
import app.tivi.domain.interactors.UpdateRecommendedShows.Params
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.parallelForEach
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateRecommendedShows(
  private val recommendedShowsStore: Lazy<RecommendedShowsStore>,
  private val showStore: Lazy<ShowStore>,
  private val traktAuthRepository: Lazy<TraktAuthRepository>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interactor<Params, Unit>() {
  override suspend fun doWork(params: Params) {
    // If we're not logged in, we can't load the recommended shows
    if (!traktAuthRepository.value.isLoggedIn()) return

    withContext(dispatchers.io) {
      recommendedShowsStore.value.fetch(0, forceFresh = params.isUserInitiated).parallelForEach {
        try {
          showStore.value.fetch(it.showId)
        } catch (ce: CancellationException) {
          throw ce
        } catch (t: Throwable) {
          Logger.e(t) { "Error while show info: ${it.showId}" }
        }
      }
    }
  }

  data class Params(override val isUserInitiated: Boolean = false) : UserInitiatedParams
}
