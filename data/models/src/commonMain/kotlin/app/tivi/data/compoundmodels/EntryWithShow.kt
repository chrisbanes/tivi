/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
