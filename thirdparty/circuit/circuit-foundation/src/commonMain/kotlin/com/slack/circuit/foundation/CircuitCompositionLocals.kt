// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.slack.circuit.runtime.CircuitContext

/**
 * Provides the given [circuitConfig] as a [CompositionLocal] to all composables within [content].
 * Also adds any other composition locals that Circuit needs.
 */
@Composable
public fun CircuitCompositionLocals(circuitConfig: CircuitConfig, content: @Composable () -> Unit) {
  CompositionLocalProvider(
    LocalCircuitConfig provides circuitConfig,
  ) {
    content()
  }
}

internal val LocalCircuitContext = compositionLocalOf<CircuitContext?> { null }

/** CompositionLocal with a current [CircuitConfig] instance. */
public val LocalCircuitConfig: ProvidableCompositionLocal<CircuitConfig?> =
  staticCompositionLocalOf {
    null
  }
