// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.analytics

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface AnalyticsComponent {
    @ApplicationScope
    @Provides
    fun provideTiviFirebaseAnalytics(bind: TiviFirebaseAnalytics): Analytics = bind
}
