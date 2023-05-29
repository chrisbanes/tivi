// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.PaginatedEntry

interface PaginatedEntryDao<EC : PaginatedEntry, LI : EntryWithShow<EC>> : EntryDao<EC, LI> {
    fun deletePage(page: Int)
    fun getLastPage(): Int?
}

fun <EC : PaginatedEntry, LI : EntryWithShow<EC>> PaginatedEntryDao<EC, LI>.updatePage(
    page: Int,
    entities: List<EC>,
) {
    deletePage(page)
    upsert(entities)
}
