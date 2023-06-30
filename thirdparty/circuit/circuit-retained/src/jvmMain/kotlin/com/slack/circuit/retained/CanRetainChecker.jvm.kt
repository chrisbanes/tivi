// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.retained

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** No-op on the JVM! */
@Composable
internal actual fun rememberCanRetainChecker(): () -> Boolean {
  return remember { { false } }
}
