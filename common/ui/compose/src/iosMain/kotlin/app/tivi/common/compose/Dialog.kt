// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DrawerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import org.jetbrains.skiko.SkikoKey

/**
 * This was mostly copied from
 * https://github.com/Syer10/compose-material-dialogs/blob/93e6f5f7c2d7891472271c9e2a9db230f8827b3c/core/src/iosMain/kotlin/com/vanpra/composematerialdialogs/IosUtils.kt
 */

@Composable
actual fun Dialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit,
) {
    Popup(
        onDismissRequest = onDismissRequest,
        popupPositionProvider = IosPopupPositionProvider,
        focusable = true,
        onKeyEvent = {
            if (properties.dismissOnBackPress && it.type == KeyEventType.KeyDown &&
                it.nativeKeyEvent.key == SkikoKey.KEY_ESCAPE
            ) {
                onDismissRequest()
                true
            } else {
                false
            }
        },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DrawerDefaults.scrimColor)
                        .let { m ->
                            if (properties.dismissOnClickOutside) {
                                m.pointerInput(onDismissRequest) {
                                    detectTapGestures(onTap = { onDismissRequest() })
                                }
                            } else {
                                m
                            }
                        },
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut(),
            ) {
                content()
            }
        }
    }
}

private object IosPopupPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}
