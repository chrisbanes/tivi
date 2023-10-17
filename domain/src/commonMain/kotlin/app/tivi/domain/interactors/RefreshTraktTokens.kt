// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.traktauth.AuthState
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.domain.Interactor
import me.tatarka.inject.annotations.Inject

@Inject
class RefreshTraktTokens(
  private val traktAuthRepository: TraktAuthRepository,
) : Interactor<Unit, AuthState?>() {
  override suspend fun doWork(params: Unit) = traktAuthRepository.refreshTokens()
}
