// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktusers

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface TraktUsersBinds {
    @ApplicationScope
    @Provides
    fun provideTraktUsersDataSource(bind: TraktUsersDataSource): UsersDataSource = bind
}
