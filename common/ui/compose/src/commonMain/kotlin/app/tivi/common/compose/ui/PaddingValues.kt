// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp

fun PaddingValues.copy(
  copyStart: Boolean = true,
  copyTop: Boolean = true,
  copyEnd: Boolean = true,
  copyBottom: Boolean = true,
  layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): PaddingValues = PaddingValues(
  start = if (copyStart) calculateStartPadding(layoutDirection) else 0.dp,
  top = if (copyTop) calculateTopPadding() else 0.dp,
  end = if (copyEnd) calculateEndPadding(layoutDirection) else 0.dp,
  bottom = if (copyBottom) calculateBottomPadding() else 0.dp,
)

fun PaddingValues.plus(
  plus: PaddingValues,
  layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): PaddingValues = PaddingValues(
  start = calculateStartPadding(layoutDirection) + plus.calculateStartPadding(layoutDirection),
  top = calculateTopPadding() + plus.calculateTopPadding(),
  end = calculateEndPadding(layoutDirection) + plus.calculateEndPadding(layoutDirection),
  bottom = calculateBottomPadding() + plus.calculateBottomPadding(),
)

fun PaddingValues.minus(
  other: PaddingValues,
  layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): PaddingValues = PaddingValues(
  start = (calculateStartPadding(layoutDirection) - other.calculateStartPadding(layoutDirection)).coerceAtLeast(0.dp),
  top = (calculateTopPadding() - other.calculateTopPadding()).coerceAtLeast(0.dp),
  end = (calculateEndPadding(layoutDirection) - other.calculateEndPadding(layoutDirection)).coerceAtLeast(0.dp),
  bottom = (calculateBottomPadding() - other.calculateBottomPadding()).coerceAtLeast(0.dp),
)

fun PaddingValues.coerceAtMost(
  paddingValues: PaddingValues,
  layoutDirection: LayoutDirection = LayoutDirection.Ltr,
): PaddingValues = PaddingValues(
  start = calculateStartPadding(layoutDirection).coerceAtMost(paddingValues.calculateStartPadding(layoutDirection)),
  top = calculateTopPadding().coerceAtMost(paddingValues.calculateTopPadding()),
  end = calculateEndPadding(layoutDirection).coerceAtMost(paddingValues.calculateEndPadding(layoutDirection)),
  bottom = calculateBottomPadding().coerceAtMost(paddingValues.calculateBottomPadding()),
)
