// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import com.slack.circuit.runtime.Navigator

internal expect class GestureNavDecoration(
    navigator: Navigator,
) : NavDecorationWithPrevious
