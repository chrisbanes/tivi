// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.core.analytics.Analytics
import app.tivi.data.traktauth.TraktLoginAction
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.data.traktauth.TraktRefreshTokenAction
import app.tivi.util.SetCrashReportingEnabledAction
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class IosApplicationComponent(
  override val analytics: Analytics,
  override val traktRefreshTokenActionProvider: (TraktOAuthInfo) -> TraktRefreshTokenAction,
  private val traktLoginActionProvider: (TraktOAuthInfo) -> TraktLoginAction,
  override val setCrashReportingEnabledAction: SetCrashReportingEnabledAction,
) : SharedApplicationComponent,
  ProdApplicationComponent {

  @Provides
  @ApplicationScope
  fun provideLoginToTraktInteractor(info: TraktOAuthInfo): TraktLoginAction {
    return traktLoginActionProvider(info)
  }

  companion object
}
