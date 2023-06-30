// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.retained

import androidx.compose.runtime.Composable

/** Checks whether or not we can retain in the current composable context. */
@Composable internal expect fun rememberCanRetainChecker(): () -> Boolean
