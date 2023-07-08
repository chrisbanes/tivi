// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.runtime.Composable

@Composable
actual fun decayAnimationSpec(): DecayAnimationSpec<Float> = rememberSplineBasedDecay()
