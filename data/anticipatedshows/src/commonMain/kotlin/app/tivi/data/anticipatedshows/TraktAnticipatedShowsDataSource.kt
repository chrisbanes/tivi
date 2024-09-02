// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.anticipatedshows

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.model.TraktShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.AnticipatedShowEntry
import app.tivi.data.models.TiviShow
import app.tivi.trakt.TraktShowsApiExtra
import me.tatarka.inject.annotations.Inject

@Inject
class TraktAnticipatedShowsDataSource(
  private val showService: Lazy<TraktShowsApiExtra>,
  showMapper: TraktShowToTiviShow,
) : AnticipatedShowsDataSource {
  private val entryMapper = IndexedMapper<TraktShow, AnticipatedShowEntry> { index, _ ->
    AnticipatedShowEntry(showId = 0, pageOrder = index, page = 0)
  }

  private val resultsMapper = pairMapperOf(showMapper, entryMapper)

  override suspend operator fun invoke(
    page: Int,
    pageSize: Int,
  ): List<Pair<TiviShow, AnticipatedShowEntry>> =
    showService.value
      .getAnticipated(page = page + 1, limit = pageSize, extended = TraktExtended.NO_SEASONS)
      .mapNotNull { it.show }
      .let { resultsMapper(it) }
}
