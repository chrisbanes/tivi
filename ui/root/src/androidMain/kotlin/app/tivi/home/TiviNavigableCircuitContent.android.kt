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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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

    val activeContentProviders = backstack.buildCircuitContentProviders(
        navigator = navigator,
        circuitConfig = circuitConfig,
        unavailableRoute = unavailableRoute,
    )

    val current = activeContentProviders.first()
    val previous = activeContentProviders.getOrNull(1)

    var backStackAnimationEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(backstack) {
        snapshotFlow { backstack.isAtRoot }
            .collect { atRoot ->
                backStackAnimationEnabled = !atRoot
            }
    }

    var lastPopViaSwipe by remember { mutableStateOf(false) }

    BackHandler(
        animatedEnabled = backStackAnimationEnabled,
        onBackProgress = { current.swipeProgress = it },
        onBackInvoked = {
            lastPopViaSwipe = current.swipeProgress != 0f
            navigator.pop()
        },
    )

    Box(modifier = modifier) {
        previous?.let { cp ->
            // movableContentOf needs to be attached to the composition otherwise it is cleaned up.
            // Ideally we wouldn't call PreviousContent when `showPrevious` is false, but then we
            // lose all of the presenter state (as the movable content is cleaned-up).
            Box(modifier = Modifier.zIndex(0f)) {
                val values = providedValues[cp.backStackRecord]?.provideValues()
                val providedLocals = remember(values) {
                    values?.toTypedArray() ?: emptyArray()
                }
                CompositionLocalProvider(*providedLocals) {
                    cp.content(cp.backStackRecord)
                }
            }
        }

        val backStackDepth = activeContentProviders.size
        var prevStackDepth by remember { mutableStateOf(backStackDepth) }
        val diff = backStackDepth - prevStackDepth

        SideEffect {
            prevStackDepth = backStackDepth
        }

        AnimatedContent(
            targetState = current,
            transitionSpec = {
                // Mirror the forward and backward transitions of activities in Android 33
                when {
                    // adding to back stack
                    diff > 0 -> {
                        (slideInHorizontally(tween(), SlightlyRight) + fadeIn()) with
                            (slideOutHorizontally(tween(), SlightlyLeft) + fadeOut())
                    }

                    // come back from back stack
                    diff < 0 -> {
                        if (lastPopViaSwipe) {
                            // This is basically a no-op enter transition. We can't use
                            // EnterTransition.None as that then stops the exit transition working.
                            // 🤷
                            fadeIn(tween(), initialAlpha = 0.98f) with fadeOut()
                        } else {
                            (slideInHorizontally(tween(), SlightlyLeft) + fadeIn()) with
                                slideOutHorizontally(tween(), SlightlyRight) + fadeOut()
                        }
                    }

                    // Root reset. Crossfade
                    else -> fadeIn() with fadeOut()
                }.using(
                    // Disable clipping since the faded slide-in/out should
                    // be displayed out of bounds.
                    SizeTransform(clip = false),
                )
            },
            label = "",
            modifier = Modifier.zIndex(1f),
        ) { contentProvider ->
            Box(
                modifier = Modifier
                    .predictiveBackMotionTreatment(MaterialTheme.shapes.extraLarge) {
                        contentProvider.swipeProgress
                    },
            ) {
                val values = providedValues[contentProvider.backStackRecord]?.provideValues()
                val providedLocals = remember(values) {
                    values?.toTypedArray() ?: emptyArray()
                }
                CompositionLocalProvider(*providedLocals) {
                    contentProvider.content(contentProvider.backStackRecord)
                }
            }
        }
    }
}

private const val FIVE_PERCENT = 0.05f
private val SlightlyRight = { width: Int -> (width * FIVE_PERCENT).toInt() }
private val SlightlyLeft = { width: Int -> 0 - (width * FIVE_PERCENT).toInt() }

private fun Modifier.predictiveBackMotionTreatment(
    shape: Shape,
    progressProvider: () -> Float,
): Modifier = graphicsLayer {
    val progress = progressProvider()
    // If we're at progress 0f, skip setting any parameters
    if (progress == 0f) return@graphicsLayer

    translationX = -(8.dp * progress).toPx()
    shadowElevation = 6.dp.toPx()

    val scale = lerp(1f, 0.9f, progress.absoluteValue)
    scaleX = scale
    scaleY = scale
    transformOrigin = TransformOrigin(
        pivotFractionX = if (progress > 0) 1f else 0f,
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
