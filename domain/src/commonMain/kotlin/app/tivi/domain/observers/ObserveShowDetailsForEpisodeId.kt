// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.daos.EpisodesDao
import app.tivi.data.models.TiviShow
import app.tivi.data.shows.ShowStore
import app.tivi.domain.SubjectInteractor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ObserveShowDetailsForEpisodeId(
  private val showStore: ShowStore,
  private val episodesDao: EpisodesDao,
  private val dispatchers: AppCoroutineDispatchers,
) : SubjectInteractor<ObserveShowDetailsForEpisodeId.Params, TiviShow>() {
  override fun createObservable(params: Params): Flow<TiviShow> {
    return episodesDao.observeShowIdForEpisodeId(params.episodeId)
      .flatMapLatest { showId ->
        showStore.stream(StoreReadRequest.cached(showId, refresh = false))
          .filter { it is StoreReadResponse.Data }
          .map { it.requireData() }
      }
      .flowOn(dispatchers.io)
  }

  data class Params(val episodeId: Long)
}
