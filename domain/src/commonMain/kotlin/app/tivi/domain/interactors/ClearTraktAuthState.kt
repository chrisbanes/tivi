// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.domain.ResultInteractor
import me.tatarka.inject.annotations.Inject

@Inject
class ClearTraktAuthState(
    private val traktAuthRepository: TraktAuthRepository,
) : ResultInteractor<Unit, Unit>() {
    override suspend fun doWork(params: Unit) {
        traktAuthRepository.clearAuth()
    }
}
