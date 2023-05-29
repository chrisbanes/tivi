// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.Season
import kotlinx.coroutines.flow.Flow

interface SeasonsDao : EntityDao<Season> {

    fun seasonsWithEpisodesForShowId(showId: Long): Flow<List<SeasonWithEpisodesAndWatches>>

    fun observeSeasonWithId(id: Long): Flow<Season>

    fun seasonsForShowId(showId: Long): List<Season>

    fun deleteWithShowId(showId: Long)

    fun seasonWithId(id: Long): Season?

    fun traktIdForId(id: Long): Int?

    fun seasonWithTraktId(traktId: Int): Season?

    fun showPreviousSeasonIds(seasonId: Long): List<Long>

    fun updateSeasonIgnoreFlag(seasonId: Long, ignored: Boolean)

    fun seasonWithShowIdAndNumber(showId: Long, number: Int): Season?
}
