// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.appinitializers

import app.tivi.core.perf.Tracer
import me.tatarka.inject.annotations.Inject

@Inject
class AppInitializers(
    private val initializers: Set<AppInitializer>,
    private val tracer: Tracer,
) {
    fun init() {
        tracer.trace("AppInitializers") {
            for (initializer in initializers) {
                initializer.init()
            }
        }
    }
}
