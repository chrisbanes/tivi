// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveTraktAuthState(
    private val traktAuthRepository: TraktAuthRepository,
) : SubjectInteractor<Unit, TraktAuthState>() {
    override fun createObservable(params: Unit): Flow<TraktAuthState> {
        return traktAuthRepository.state
    }
}
