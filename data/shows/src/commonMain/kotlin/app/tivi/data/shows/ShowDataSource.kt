// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.shows

import app.tivi.data.models.TiviShow

interface ShowDataSource {
    suspend fun getShow(show: TiviShow): TiviShow
}
