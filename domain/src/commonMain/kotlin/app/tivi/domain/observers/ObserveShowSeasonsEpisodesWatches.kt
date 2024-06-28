// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.models.Season
import app.tivi.domain.SubjectInteractor
import app.tivi.settings.TiviPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveShowSeasonsEpisodesWatches(
  private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
  private val preferences: TiviPreferences,
) : SubjectInteractor<ObserveShowSeasonsEpisodesWatches.Params, List<SeasonWithEpisodesAndWatches>>() {

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun createObservable(params: Params): Flow<List<SeasonWithEpisodesAndWatches>> {
    return preferences.ignoreSpecials.flow.flatMapLatest { ignoreSpecials ->
      if (ignoreSpecials) {
        seasonsEpisodesRepository
          .observeSeasonsWithEpisodesWatchedForShow(params.showId)
          .map { seasonsWithEpisodes ->
            seasonsWithEpisodes.map { seasonWithEpisodes ->
              if (seasonWithEpisodes.season.number == Season.NUMBER_SPECIALS) {
                seasonWithEpisodes.copy(
                  season = seasonWithEpisodes.season.copy(ignored = true),
                )
              } else {
                seasonWithEpisodes
              }
            }
          }
      } else {
        // If we're not ignoring specials, just use the flow as-is
        seasonsEpisodesRepository.observeSeasonsWithEpisodesWatchedForShow(params.showId)
      }
    }
  }

  data class Params(val showId: Long)
}
