// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import androidx.compose.ui.unit.Density
import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.appinitializers.AppInitializers
import app.tivi.core.analytics.Analytics
import app.tivi.data.traktauth.TraktLoginAction
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.data.traktauth.TraktRefreshTokenAction
import kotlin.experimental.ExperimentalNativeApi
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

@Component
@ApplicationScope
abstract class IosApplicationComponent(
    override val analyticsProvider: () -> Analytics,
    override val traktRefreshTokenActionProvider: (TraktOAuthInfo) -> TraktRefreshTokenAction,
    private val traktLoginActionProvider: (TraktOAuthInfo) -> TraktLoginAction,
) : SharedApplicationComponent {

    abstract val initializers: AppInitializers

    @OptIn(ExperimentalNativeApi::class)
    @ApplicationScope
    @Provides
    fun provideApplicationId(): ApplicationInfo = ApplicationInfo(
        packageName = NSBundle.mainBundle.bundleIdentifier ?: "app.tivi.client",
        debugBuild = Platform.isDebugBinary,
        flavor = Flavor.Standard,
        versionName = NSBundle.mainBundle.infoDictionary
            ?.get("CFBundleShortVersionString") as? String
            ?: "",
        versionCode = (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)
            ?.toIntOrNull()
            ?: 0,
    )

    @Provides
    fun provideNsUserDefaults(): NSUserDefaults = NSUserDefaults.standardUserDefaults

    @Provides
    @ApplicationScope
    fun provideLoginToTraktInteractor(info: TraktOAuthInfo): TraktLoginAction {
        return traktLoginActionProvider(info)
    }

    @Provides
    fun provideDensity(): Density = Density(density = 1f) // FIXME

    companion object
}
