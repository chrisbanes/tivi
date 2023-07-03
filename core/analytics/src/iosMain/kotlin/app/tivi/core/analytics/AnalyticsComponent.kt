// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.analytics

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface AnalyticsComponent {
    var analyticsProvider: () -> Analytics

    @Provides
    @ApplicationScope
    fun provideAnalytics(): Analytics = analyticsProvider()
}
