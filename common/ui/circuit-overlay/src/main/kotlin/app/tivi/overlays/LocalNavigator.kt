// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.overlays

import androidx.compose.runtime.staticCompositionLocalOf
import com.slack.circuit.runtime.Navigator

/**
 * Yes, I don't like this either. I'm a bit stuck though due to
 * https://github.com/slackhq/circuit/issues/653
 */
val LocalNavigator = staticCompositionLocalOf<Navigator> { Navigator.NoOp }
