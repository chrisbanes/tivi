// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.core.analytics.AnalyticsComponent
import app.tivi.core.perf.PerformanceComponent
import app.tivi.settings.PreferencesComponent
import app.tivi.util.LoggerComponent
import app.tivi.util.PowerControllerComponent

interface CoreComponent :
    AnalyticsComponent,
    LoggerComponent,
    PerformanceComponent,
    PowerControllerComponent,
    PreferencesComponent
