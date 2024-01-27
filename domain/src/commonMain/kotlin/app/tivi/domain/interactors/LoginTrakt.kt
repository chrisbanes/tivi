// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.traktauth.AuthState
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.domain.Interactor
import me.tatarka.inject.annotations.Inject

@Inject
class LoginTrakt(
  traktAuthRepository: Lazy<TraktAuthRepository>,
) : Interactor<Unit, AuthState?>() {
  private val traktAuthRepository by traktAuthRepository

  override suspend fun doWork(params: Unit) = traktAuthRepository.login()
}
