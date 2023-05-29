// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.followedshows.FollowedShowsRepository
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveShowFollowStatus(
    private val followedShowsRepository: FollowedShowsRepository,
) : SubjectInteractor<ObserveShowFollowStatus.Params, Boolean>() {

    override fun createObservable(params: Params): Flow<Boolean> {
        return followedShowsRepository.observeIsShowFollowed(params.showId)
    }

    data class Params(val showId: Long)
}
