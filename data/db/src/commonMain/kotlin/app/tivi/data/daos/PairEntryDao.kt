// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.MultipleEntry
import kotlinx.coroutines.flow.Flow

/**
 * This interface represents a DAO which contains entities which are part of a collective list for a given show.
 */
interface PairEntryDao<EC : MultipleEntry, LI : EntryWithShow<EC>> : EntityDao<EC> {
    fun entriesWithShowsObservable(showId: Long): Flow<List<LI>>
    fun deleteWithShowId(showId: Long)
}
