// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.models.TraktUser
import app.tivi.data.traktusers.TraktUsersRepository
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveUserDetails(
    private val repository: TraktUsersRepository,
) : SubjectInteractor<ObserveUserDetails.Params, TraktUser?>() {

    override fun createObservable(params: Params): Flow<TraktUser?> {
        return repository.observeUser(params.username)
    }

    data class Params(val username: String)
}
