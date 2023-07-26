// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.appinitializers.AppInitializer
import me.tatarka.inject.annotations.Inject

@Inject
class LoggerInitializer(
    private val logger: Logger,
    private val applicationInfo: ApplicationInfo,
) : AppInitializer {
    override fun initialize() {
        logger.setup(
            debugMode = when {
                applicationInfo.debugBuild -> true
                applicationInfo.flavor == Flavor.Qa -> true
                else -> false
            },
        )
    }
}
