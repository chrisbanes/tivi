// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.compoundmodels

import app.tivi.data.models.Entry
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PopularShowEntry
import app.tivi.data.models.RecommendedShowEntry
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.TiviShow
import app.tivi.data.models.TrendingShowEntry
import app.tivi.data.models.WatchedShowEntry

data class EntryWithShow<ET : Entry>(
    val entry: ET,
    val show: TiviShow,
)

typealias FollowedShowEntryWithShow = EntryWithShow<FollowedShowEntry>
typealias PopularEntryWithShow = EntryWithShow<PopularShowEntry>
typealias RecommendedEntryWithShow = EntryWithShow<RecommendedShowEntry>
typealias RelatedShowEntryWithShow = EntryWithShow<RelatedShowEntry>
typealias TrendingEntryWithShow = EntryWithShow<TrendingShowEntry>
typealias WatchedShowEntryWithShow = EntryWithShow<WatchedShowEntry>
