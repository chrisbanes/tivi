// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.appinitializers.AppInitializers
import app.tivi.core.analytics.Analytics
import app.tivi.data.traktauth.TraktLoginAction
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.data.traktauth.TraktRefreshTokenAction
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class IosApplicationComponent(
    override val analyticsProvider: () -> Analytics,
    override val traktRefreshTokenActionProvider: (TraktOAuthInfo) -> TraktRefreshTokenAction,
    private val traktLoginActionProvider: (TraktOAuthInfo) -> TraktLoginAction,
) : SharedApplicationComponent, QaApplicationComponent {

    abstract val initializers: AppInitializers

    @Provides
    @ApplicationScope
    fun provideLoginToTraktInteractor(info: TraktOAuthInfo): TraktLoginAction {
        return traktLoginActionProvider(info)
    }

    companion object
}
