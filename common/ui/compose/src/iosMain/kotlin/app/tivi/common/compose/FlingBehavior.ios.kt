// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun decayAnimationSpec(): DecayAnimationSpec<Float> {
  return remember { exponentialDecay(frictionMultiplier = 0.93f) }
}
