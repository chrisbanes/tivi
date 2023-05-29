// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.models.TraktUser
import kotlinx.coroutines.flow.Flow

interface UserDao : EntityDao<TraktUser> {

    fun observeMe(): Flow<TraktUser?>

    fun observeTraktUser(username: String): Flow<TraktUser?>

    fun getUser(username: String): TraktUser?

    fun getId(username: String): Long?

    fun deleteWithUsername(username: String)

    fun deleteMe()

    fun deleteAll()
}
