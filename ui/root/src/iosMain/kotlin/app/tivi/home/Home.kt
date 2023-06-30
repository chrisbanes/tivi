// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.core.analytics.Analytics
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitConfig

fun HomeViewController(
    onRootPop: () -> Unit,
    onOpenSettings: () -> Unit,
    imageLoader: ImageLoader,
    tiviDateFormatter: TiviDateFormatter,
    tiviTextCreator: TiviTextCreator,
    circuitConfig: CircuitConfig,
    analytics: Analytics,
    preferences: TiviPreferences,
) = ComposeUIViewController {
    CompositionLocalProvider(
        LocalImageLoader provides imageLoader,
        LocalTiviDateFormatter provides tiviDateFormatter,
        LocalTiviTextCreator provides tiviTextCreator,
    ) {
        CircuitCompositionLocals(circuitConfig) {
            TiviContent(
                onRootPop = onRootPop,
                onOpenSettings = onOpenSettings,
                analytics = analytics,
                preferences = preferences,
            )
        }
    }
}
