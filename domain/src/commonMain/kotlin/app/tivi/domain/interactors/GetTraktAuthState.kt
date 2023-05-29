// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.ResultInteractor
import me.tatarka.inject.annotations.Inject

@Inject
class GetTraktAuthState(
    private val traktAuthRepository: TraktAuthRepository,
) : ResultInteractor<Unit, TraktAuthState>() {
    override suspend fun doWork(params: Unit): TraktAuthState {
        return traktAuthRepository.state.value
    }
}
