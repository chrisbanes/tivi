// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktHistoryItem
import app.tivi.data.models.Episode
import me.tatarka.inject.annotations.Inject

@Inject
class TraktHistoryEntryToEpisode(
    private val mapper: TraktEpisodeToEpisode,
) : Mapper<TraktHistoryItem, Episode> {

    override fun map(from: TraktHistoryItem) = mapper.map(requireNotNull(from.episode))
}
