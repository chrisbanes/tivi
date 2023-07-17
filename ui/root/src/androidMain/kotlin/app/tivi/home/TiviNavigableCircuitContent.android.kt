// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedDispatcher
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.tivi.animations.lerp
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.isAtRoot
import com.slack.circuit.backstack.isEmpty
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import kotlin.math.absoluteValue

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("NewApi")
@Composable
actual fun TiviNavigableCircuitContent(
    navigator: Navigator,
    backstack: SaveableBackStack,
    modifier: Modifier,
    circuitConfig: CircuitConfig,
    providedValues: Map<out BackStack.Record, ProvidedValues>,
    unavailableRoute: @Composable (screen: Screen, modifier: Modifier) -> Unit,
) {
    if (Build.VERSION.SDK_INT < 34 || backstack.isEmpty) {
        // On API 33 or below, just use the default implementation
        NavigableCircuitContent(
            navigator = navigator,
            backstack = backstack,
            modifier = modifier,
            circuitConfig = circuitConfig,
            providedValues = providedValues,
        )
        return
    }

    GestureDrivenNavigableCircuitContent(
        navigator = navigator,
        backstack = backstack,
        modifier = modifier,
        circuitConfig = circuitConfig,
        providedValues = providedValues,
        unavailableRoute = unavailableRoute,
        transitionSpec = { diff ->
            // Mirror the forward and backward transitions of activities in Android 33
            when {
                // adding to back stack
                diff > 0 -> {
                    (slideInHorizontally(tween(), SlightlyRight) + fadeIn()) with
                        (slideOutHorizontally(tween(), SlightlyLeft) + fadeOut())
                }

                // come back from back stack
                diff < 0 -> {
                    if (initialState.poppedViaGesture) {
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
    ) {
        var swipeProgress by remember { mutableStateOf(0f) }

        BackHandler(
            animatedEnabled = !backstack.isAtRoot,
            onBackProgress = { swipeProgress = it },
            onBackInvoked = {
                poppedViaGesture = true
                navigator.pop()
            },
        )

        Box(
            modifier = Modifier.predictiveBackMotion(
                shape = MaterialTheme.shapes.extraLarge,
                progress = { swipeProgress },
            ),
        ) {
            content(backStackRecord)
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
                Log.d("OnBackAnimationCallback", "onProgress: ${backEvent.progress}")

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
