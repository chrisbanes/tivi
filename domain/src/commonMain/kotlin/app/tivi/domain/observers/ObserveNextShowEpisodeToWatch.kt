// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.EpisodeWithSeasonWithShow
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.SubjectInteractor
import app.tivi.extensions.flatMapLatestNullable
import app.tivi.extensions.mapNullable
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveNextShowEpisodeToWatch(
    private val watchedShowDao: WatchedShowDao,
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
) : SubjectInteractor<Unit, EpisodeWithSeasonWithShow?>() {

    override fun createObservable(params: Unit): Flow<EpisodeWithSeasonWithShow?> {
        return watchedShowDao.observeNextShowToWatch().flatMapLatestNullable { nextShow ->
            seasonsEpisodesRepository.observeNextEpisodeToWatch(nextShow.id).mapNullable {
                EpisodeWithSeasonWithShow(it.episode, it.season, nextShow)
            }
        }
    }
}
