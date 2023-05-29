// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktUserListItem
import app.tivi.data.models.FollowedShowEntry
import me.tatarka.inject.annotations.Inject

@Inject
class TraktListItemToFollowedShowEntry : Mapper<TraktUserListItem, FollowedShowEntry> {
    override fun map(from: TraktUserListItem) = FollowedShowEntry(
        showId = 0,
        followedAt = from.listedAt,
        traktId = from.id,
    )
}
