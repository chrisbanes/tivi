// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import androidx.compose.ui.unit.Density
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIScreen

interface SharedIosApplicationComponent : SharedApplicationComponent {
    @Provides
    fun provideNsUserDefaults(): NSUserDefaults = NSUserDefaults.standardUserDefaults

    @Provides
    fun provideDensity(): Density = Density(density = UIScreen.mainScreen.scale.toFloat())
}
