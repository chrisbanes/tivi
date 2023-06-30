// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import androidx.compose.ui.unit.Density
import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.appinitializers.AppInitializers
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSBundle

@Component
@ApplicationScope
abstract class IosApplicationComponent : SharedApplicationComponent {
    abstract val initializers: AppInitializers

    @ApplicationScope
    @Provides
    fun provideApplicationId(): ApplicationInfo = ApplicationInfo(
        packageName = NSBundle.mainBundle.bundleIdentifier ?: "empty.bundle.id",
        debugBuild = Platform.isDebugBinary,
        flavor = Flavor.Standard,
    )

    @Provides
    fun provideDensity(): Density = Density(density = 1f) // FIXME

    companion object
}
