// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.models.ActionDate
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class ChangeSeasonWatchedStatus(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<ChangeSeasonWatchedStatus.Params, Unit>() {

    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            when (params.action) {
                Action.WATCHED -> {
                    seasonsEpisodesRepository.markSeasonWatched(
                        params.seasonId,
                        params.onlyAired,
                        params.actionDate,
                    )
                }

                Action.UNWATCH -> {
                    seasonsEpisodesRepository.markSeasonUnwatched(params.seasonId)
                }
            }
        }
    }

    data class Params(
        val seasonId: Long,
        val action: Action,
        val onlyAired: Boolean = true,
        val actionDate: ActionDate = ActionDate.NOW,
    )

    enum class Action { WATCHED, UNWATCH }
}
