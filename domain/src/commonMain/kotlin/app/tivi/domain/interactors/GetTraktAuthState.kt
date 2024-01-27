// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.Interactor
import me.tatarka.inject.annotations.Inject

@Inject
class GetTraktAuthState(
  traktAuthRepository: Lazy<TraktAuthRepository>,
) : Interactor<Unit, TraktAuthState>() {
  private val traktAuthRepository by traktAuthRepository

  override suspend fun doWork(params: Unit): TraktAuthState {
    return traktAuthRepository.state.value
  }
}
