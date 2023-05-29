// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.appinitializers.AppInitializer
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface ImageLoadingComponent {
    @Provides
    @IntoSet
    fun provideCoilInitializer(bind: CoilAppInitializer): AppInitializer = bind
}
