// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.daos.UserDao
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class ClearUserDetails(
  userDao: Lazy<UserDao>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interactor<ClearUserDetails.Params, Unit>() {
  private val userDao by userDao

  override suspend fun doWork(params: Params) {
    withContext(dispatchers.io) {
      when (params.username) {
        "me" -> userDao.deleteMe()
        else -> userDao.deleteWithUsername(params.username)
      }
    }
  }

  data class Params(val username: String)
}
