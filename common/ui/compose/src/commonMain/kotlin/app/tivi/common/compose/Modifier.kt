// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.ui.Modifier

inline fun Modifier.thenIf(
  condition: Boolean,
  whenFalse: Modifier.() -> Modifier = { this },
  whenTrue: Modifier.() -> Modifier,
): Modifier = if (condition) whenTrue() else whenFalse()
