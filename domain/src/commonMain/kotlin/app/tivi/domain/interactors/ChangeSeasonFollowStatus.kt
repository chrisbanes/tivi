// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class ChangeSeasonFollowStatus(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<ChangeSeasonFollowStatus.Params, Unit>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            when (params.action) {
                Action.FOLLOW -> {
                    seasonsEpisodesRepository.markSeasonFollowed(params.seasonId)
                }

                Action.IGNORE -> {
                    seasonsEpisodesRepository.markSeasonIgnored(params.seasonId)
                }

                Action.IGNORE_PREVIOUS -> {
                    seasonsEpisodesRepository.markPreviousSeasonsIgnored(params.seasonId)
                }
            }
        }
    }

    data class Params(
        val seasonId: Long,
        val action: Action,
    )

    enum class Action { FOLLOW, IGNORE, IGNORE_PREVIOUS }
}
