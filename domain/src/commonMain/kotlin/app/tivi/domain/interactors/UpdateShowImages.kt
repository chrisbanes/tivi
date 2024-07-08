// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.showimages.ShowImagesStore
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.domain.UserInitiatedParams
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateShowImages(
  private val showImagesStore: Lazy<ShowImagesStore>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interactor<UpdateShowImages.Params, Unit>() {
  override suspend fun doWork(params: Params) {
    withContext(dispatchers.io) {
      showImagesStore.value.fetch(params.showId, params.isUserInitiated)
    }
  }

  data class Params(val showId: Long, override val isUserInitiated: Boolean) : UserInitiatedParams
}
