// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.animations

fun lerp(
    startValue: Float,
    endValue: Float,
    fraction: Float,
) = startValue + fraction * (endValue - startValue)
