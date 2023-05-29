// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.followedshows

import me.tatarka.inject.annotations.Provides

interface FollowedShowsBinds {
    @Provides
    fun provideTraktFollowedShowsDataSource(
        bind: TraktFollowedShowsDataSource,
    ): FollowedShowsDataSource = bind
}
