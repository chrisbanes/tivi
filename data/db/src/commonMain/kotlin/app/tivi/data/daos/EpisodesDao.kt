// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.models.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface EpisodesDao : EntityDao<Episode> {

    fun episodesWithSeasonId(seasonId: Long): List<Episode>

    fun deleteWithSeasonId(seasonId: Long)

    fun episodeWithTraktId(traktId: Int): Episode?

    fun episodeWithTmdbId(tmdbId: Int): Episode?

    fun episodeWithId(id: Long): Episode?

    fun episodeTraktIdForId(id: Long): Int?

    fun episodeIdWithTraktId(traktId: Int): Long?

    fun episodeWithIdObservable(id: Long): Flow<EpisodeWithSeason>

    fun showIdForEpisodeId(episodeId: Long): Long

    fun observeNextEpisodeToWatch(showId: Long): Flow<EpisodeWithSeason?> = emptyFlow()
}
