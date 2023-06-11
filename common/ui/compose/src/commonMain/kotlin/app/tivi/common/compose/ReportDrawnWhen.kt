// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable

@Composable
expect fun ReportDrawnWhen(predicate: () -> Boolean)
