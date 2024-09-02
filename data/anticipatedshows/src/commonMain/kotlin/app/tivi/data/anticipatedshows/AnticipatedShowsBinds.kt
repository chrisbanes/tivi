// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.anticipatedshows

import me.tatarka.inject.annotations.Provides

interface AnticipatedShowsBinds {
  @Provides
  fun provideTraktAnticipatedShowsDataSource(
    bind: TraktAnticipatedShowsDataSource,
  ): AnticipatedShowsDataSource = bind
}
