// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Implements a suitable min-width for Android dialog content.
 *
 * Matches the values used in the platform default dialog themes `Theme.Material.Dialog.MinWidth`
 * and `Theme.Material.Dialog.Alert`. Unfortunately the necessary attributes used in the themes
 * are private, so we can't read them from the theme (and AppCompat duplicates them too).
 *
 * The values in question can be found here:
 * https://cs.android.com/search?q=dialog_min_width%20file:dimens.xml&sq=&ss=android%2Fplatform%2Fsuperproject:frameworks%2Fbase%2F
 *
 * This primarily exists to workaround https://issuetracker.google.com/issues/221643630, which
 * requires the workaround of using `DialogProperties(usePlatformDefaultWidth = false)`.
 *
 * @param clampMaxWidth Whether to clamp the maximum width to the same value. This is useful for
 * Compose content as fillMaxWidth() (or similar) is frequently used, which then stretches the
 * dialog to fill the screen width.
 */
fun Modifier.androidMinWidthDialogSize(
    clampMaxWidth: Boolean = false,
): Modifier = composed {
    val configuration = LocalConfiguration.current
    val density = LocalContext.current.resources.displayMetrics.density

    val displayWidth = (configuration.screenWidthDp * density).roundToInt()
    val displayHeight = (configuration.screenHeightDp * density).roundToInt()

    val minWidthRatio: Float = when {
        configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE) -> {
            if (displayWidth > displayHeight) 0.45f else 0.72f
        }
        configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE) -> {
            if (displayWidth > displayHeight) 0.55f else 0.8f
        }
        else -> {
            if (displayWidth > displayHeight) 0.65f else 0.95f
        }
    }

    if (clampMaxWidth) {
        Modifier.width(((displayWidth * minWidthRatio) / density).dp)
    } else {
        Modifier.widthIn(min = ((displayWidth * minWidthRatio) / density).dp)
    }
}
