// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktusers

import app.tivi.data.models.TraktUser

fun interface UsersDataSource {
    suspend fun getUser(slug: String): TraktUser
}
