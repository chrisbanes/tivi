// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.compoundmodels.RelatedShowEntryWithShow
import app.tivi.data.models.RelatedShowEntry
import kotlinx.coroutines.flow.Flow

interface RelatedShowsDao : PairEntryDao<RelatedShowEntry, RelatedShowEntryWithShow> {
    fun entriesObservable(showId: Long): Flow<List<RelatedShowEntry>>
    fun deleteAll()
}
