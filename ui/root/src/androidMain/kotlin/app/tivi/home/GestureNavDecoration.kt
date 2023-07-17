// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import android.annotation.SuppressLint
import android.os.Build
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedDispatcher
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.tivi.animations.lerp
import com.slack.circuit.foundation.NavigatorDefaults
import com.slack.circuit.runtime.Navigator
import kotlin.math.absoluteValue

@SuppressLint("NewApi")
@OptIn(ExperimentalAnimationApi::class)
internal actual class GestureNavDecoration actual constructor(
    private val navigator: Navigator,
) : NavDecorationWithPrevious {

    @Composable
    override fun <T> DecoratedContent(
        arg: T,
        previous: T?,
        backStackDepth: Int,
        modifier: Modifier,
        content: @Composable (T) -> Unit,
    ) {
        if (Build.VERSION.SDK_INT < 34) {
            return NavigatorDefaults.DefaultDecoration.DecoratedContent(
                arg = arg,
                backStackDepth = backStackDepth,
                modifier = modifier,
                content = content,
            )
        }

        // Remember the previous stack depth so we know if the navigation is going "back".
        var prevStackDepth by rememberSaveable { mutableStateOf(backStackDepth) }
        SideEffect {
            prevStackDepth = backStackDepth
        }

        val poppedViaGesture = remember { mutableMapOf<T, Boolean>() }

        Box(modifier = modifier) {
            if (previous != null) {
                content(previous)
            }

            AnimatedContent(
                targetState = arg,
                transitionSpec = {
                    // Mirror the forward and backward transitions of activities in Android 33
                    when {
                        // adding to back stack
                        backStackDepth > prevStackDepth -> {
                            (slideInHorizontally(tween(), SlightlyRight) + fadeIn()) with
                                (slideOutHorizontally(tween(), SlightlyLeft) + fadeOut())
                        }

                        // come back from back stack
                        backStackDepth < prevStackDepth -> {
                            if (poppedViaGesture.getOrDefault(initialState, false)) {
                                EnterTransition.None with scaleOut(targetScale = 0.8f) + fadeOut()
                            } else {
                                slideInHorizontally(tween(), SlightlyLeft) + fadeIn() with
                                    slideOutHorizontally(tween(), SlightlyRight) + fadeOut()
                            }.apply {
                                targetContentZIndex = -1f
                            }
                        }

                        // Root reset. Crossfade
                        else -> fadeIn() with fadeOut()
                    }
                },
                modifier = modifier,
                label = "",
            ) { record ->
                var swipeProgress by remember { mutableStateOf(0f) }

                if (backStackDepth > 1) {
                    BackHandler(
                        onBackProgress = { swipeProgress = it },
                        onBackInvoked = {
                            poppedViaGesture[record] = true
                            navigator.pop()
                        },
                    )
                }

                Box(
                    modifier = Modifier.predictiveBackMotion(
                        shape = MaterialTheme.shapes.extraLarge,
                        progress = { swipeProgress },
                    ),
                ) {
                    content(record)
                }
            }
        }
    }
}

private const val FIVE_PERCENT = 0.05f
private val SlightlyRight = { width: Int -> (width * FIVE_PERCENT).toInt() }
private val SlightlyLeft = { width: Int -> 0 - (width * FIVE_PERCENT).toInt() }

/**
 *
 */
private fun Modifier.predictiveBackMotion(
    shape: Shape,
    progress: () -> Float,
): Modifier = graphicsLayer {
    val p = progress()
    // If we're at progress 0f, skip setting any parameters
    if (p == 0f) return@graphicsLayer

    translationX = -(8.dp * p).toPx()
    shadowElevation = 6.dp.toPx()

    val scale = lerp(1f, 0.9f, p.absoluteValue)
    scaleX = scale
    scaleY = scale
    transformOrigin = TransformOrigin(
        pivotFractionX = if (p > 0) 1f else 0f,
        pivotFractionY = 0.5f,
    )

    // TODO: interpolate from rectangle to shape?
    this.shape = shape
    clip = true
}

@RequiresApi(34)
@Composable
private fun BackHandler(
    onBackProgress: (Float) -> Unit,
    animatedEnabled: Boolean = true,
    onBackInvoked: () -> Unit,
) {
    val onBackInvokedDispatcher = LocalView.current.findOnBackInvokedDispatcher()
    val lastAnimatedEnabled by rememberUpdatedState(animatedEnabled)
    val lastOnBackProgress by rememberUpdatedState(onBackProgress)
    val lastOnBackInvoked by rememberUpdatedState(onBackInvoked)

    DisposableEffect(onBackInvokedDispatcher) {
        val callback = object : OnBackAnimationCallback {
            override fun onBackStarted(backEvent: BackEvent) {
                if (lastAnimatedEnabled) {
                    lastOnBackProgress(0f)
                }
            }

            override fun onBackProgressed(backEvent: BackEvent) {
                if (lastAnimatedEnabled) {
                    lastOnBackProgress(
                        when (backEvent.swipeEdge) {
                            BackEvent.EDGE_LEFT -> backEvent.progress
                            else -> -backEvent.progress
                        },
                    )
                }
            }

            override fun onBackInvoked() = lastOnBackInvoked()
        }

        onBackInvokedDispatcher?.registerOnBackInvokedCallback(
            // Circuit adds its own BackHandler() at the root, so we need to add a callback
            // with a higher priority.
            OnBackInvokedDispatcher.PRIORITY_DEFAULT + 10,
            callback,
        )

        onDispose {
            onBackInvokedDispatcher?.unregisterOnBackInvokedCallback(callback)
        }
    }
}
