// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

fun Modifier.noIndicationClickable(
  enabled: Boolean = true,
  onClickLabel: String? = null,
  role: Role? = null,
  onClick: () -> Unit,
): Modifier = composed {
  clickable(
    enabled = enabled,
    onClickLabel = onClickLabel,
    onClick = onClick,
    role = role,
    indication = null,
    interactionSource = remember { MutableInteractionSource() },
  )
}
