// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier

/**
 * Renders the given [content] with the ability to show overlays on top of it. This works by
 * exposing an [OverlayHost] via [LocalOverlayHost].
 *
 * @param modifier The modifier to be applied to the layout.
 * @param overlayHost the [OverlayHost] to use for managing overlays.
 * @param content The regular content to render. Any overlays will be rendered over them.
 */
@Composable
public fun ContentWithOverlays(
  modifier: Modifier = Modifier,
  overlayHost: OverlayHost = rememberOverlayHost(),
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(LocalOverlayHost provides overlayHost) {
    val overlayHostData by rememberUpdatedState(overlayHost.currentOverlayData)
    Box(modifier) {
      content()
      key(overlayHostData) { overlayHostData?.let { data -> data.overlay.Content(data::finish) } }
    }
  }
}
