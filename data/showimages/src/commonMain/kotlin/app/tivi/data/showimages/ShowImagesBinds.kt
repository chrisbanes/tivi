// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.showimages

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface ShowImagesBinds {
    @ApplicationScope
    @Provides
    fun bindShowImagesDataSource(bind: TmdbShowImagesDataSource): ShowImagesDataSource = bind
}
