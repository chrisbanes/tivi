// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import app.tivi.app.test.AppScenarios
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = rule.collect(
        packageName = "app.tivi",
        stableIterations = 2,
        maxIterations = 8,
    ) {
        startActivityAndWait()
        // Run through the main navigation items
        AppScenarios.mainNavigationItems(device)
    }
}
