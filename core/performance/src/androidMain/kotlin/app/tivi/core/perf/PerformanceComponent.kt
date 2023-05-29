// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.perf

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface PerformanceComponent {
    @ApplicationScope
    @Provides
    fun provideTracer(bind: AndroidTracer): Tracer = bind
}
