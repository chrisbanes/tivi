// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.Entry

/**
 * This interface represents a DAO which contains entities which are part of a single collective list.
 */
interface EntryDao<EC : Entry, LI : EntryWithShow<EC>> : EntityDao<EC> {
    fun deleteAll()
}
