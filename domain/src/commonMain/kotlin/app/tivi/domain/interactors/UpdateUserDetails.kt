// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.traktusers.TraktUsersRepository
import app.tivi.domain.Interactor
import app.tivi.domain.UserInitiatedParams
import app.tivi.domain.interactors.UpdateUserDetails.Params
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateUserDetails(
  private val repository: Lazy<TraktUsersRepository>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interactor<Params, Unit>() {
  override suspend fun doWork(params: Params) {
    withContext(dispatchers.io) {
      if (params.isUserInitiated || repository.value.needUpdate(params.username)) {
        repository.value.updateUser(params.username)
      }
    }
  }

  data class Params(val username: String, override val isUserInitiated: Boolean) : UserInitiatedParams
}
