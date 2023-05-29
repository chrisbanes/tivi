// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.appinitializers

import app.tivi.BuildConfig
import app.tivi.util.Logger
import me.tatarka.inject.annotations.Inject

@Inject
class LoggerInitializer(
    private val logger: Logger,
) : AppInitializer {
    @Suppress("KotlinConstantConditions")
    override fun init() {
        val debugMode = when {
            BuildConfig.DEBUG -> true
            BuildConfig.FLAVOR == "qa" -> true
            else -> false
        }
        logger.setup(debugMode)
    }
}
