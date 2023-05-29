// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.models.TiviShow

interface ShowFtsDao {

    fun search(filter: String): List<TiviShow>
}
