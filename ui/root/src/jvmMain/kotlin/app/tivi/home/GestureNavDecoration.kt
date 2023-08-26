// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.util.Logger
import com.slack.circuit.runtime.Navigator

internal actual class GestureNavDecoration actual constructor(
    navigator: Navigator,
    logger: Logger,
) : NavDecorationWithPrevious {
    @Composable
    override fun <T> DecoratedContent(
        arg: T,
        previous: T?,
        backStackDepth: Int,
        modifier: Modifier,
        content: @Composable (T) -> Unit,
    ) {
        // On Desktop we just use the built-in DefaultDecoration
        DefaultDecoration.DecoratedContent(
            arg = arg,
            backStackDepth = backStackDepth,
            modifier = modifier,
            content = content,
        )
    }
}
