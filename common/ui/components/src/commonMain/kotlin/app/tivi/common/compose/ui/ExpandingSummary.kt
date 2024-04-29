// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ExpandingText(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = MaterialTheme.typography.bodyMedium,
  expandable: Boolean = true,
  collapsedMaxLines: Int = 4,
  expandedMaxLines: Int = Int.MAX_VALUE,
) {
  var canTextExpand by remember(text) { mutableStateOf(true) }
  var expanded by remember { mutableStateOf(false) }

  Text(
    text = text,
    style = style,
    overflow = TextOverflow.Ellipsis,
    maxLines = if (expanded) expandedMaxLines else collapsedMaxLines,
    modifier = Modifier
      .clickable(
        enabled = expandable && canTextExpand,
        onClick = { expanded = !expanded },
      )
      .animateContentSize(animationSpec = spring())
      .then(modifier),
    onTextLayout = {
      if (!expanded) {
        canTextExpand = it.hasVisualOverflow
      }
    },
  )
}
