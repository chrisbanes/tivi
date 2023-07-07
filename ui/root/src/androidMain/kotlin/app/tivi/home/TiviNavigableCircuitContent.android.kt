// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import android.annotation.SuppressLint
import android.os.Build
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedDispatcher
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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

    var progress by remember { mutableStateOf(0f) }

    val onBackInvokedDispatcher = LocalView.current.findOnBackInvokedDispatcher()
    var backStackAnimationEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(backstack) {
        snapshotFlow { backstack.isAtRoot }
            .collect { atRoot ->
                backStackAnimationEnabled = !atRoot
            }
    }

    DisposableEffect(onBackInvokedDispatcher) {
        val callback = object : OnBackAnimationCallback {
            override fun onBackStarted(backEvent: BackEvent) {
                progress = 0f
            }

            override fun onBackProgressed(backEvent: BackEvent) {
                if (!backStackAnimationEnabled) return

                when (backEvent.swipeEdge) {
                    BackEvent.EDGE_LEFT -> {
                        progress = backEvent.progress
                    }

                    else -> {
                        progress = -backEvent.progress
                    }
                }
            }

            override fun onBackInvoked() {
                // TODO finish off animation before pop()
                navigator.pop()
                progress = 0f
            }
        }

        onBackInvokedDispatcher?.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT + 10,
            callback,
        )

        onDispose {
            onBackInvokedDispatcher?.unregisterOnBackInvokedCallback(callback)
        }
    }

    Box(modifier = modifier) {
        val activeContentProviders = buildCircuitContentProvider(
            navigator = navigator,
            backstack = backstack,
            circuitConfig = circuitConfig,
            unavailableRoute = unavailableRoute,
        )

        val topContentProvider = activeContentProviders.first().let {
            remember(it.backStackRecord.key) { it }
        }
        val previousContentProvider = activeContentProviders.getOrNull(1)?.let {
            remember(it.backStackRecord.key) { it }
        }

        val showPrevious by remember {
            derivedStateOf { progress != 0f }
        }

        if (showPrevious) {
            previousContentProvider?.let { cp ->
                Box(modifier = Modifier.zIndex(0f)) {
                    val values = providedValues[cp.backStackRecord]?.provideValues()
                    val providedLocals = remember(values) {
                        values?.toTypedArray() ?: emptyArray()
                    }
                    CompositionLocalProvider(*providedLocals) { cp.content() }
                }
            }
        }

        val shape = MaterialTheme.shapes.extraLarge
        Box(
            modifier = Modifier
                .zIndex(if (showPrevious) 1f else 0f)
                .graphicsLayer {
                    val eased = FastOutSlowInEasing.transform(progress)

                    translationX = -(48.dp * eased).toPx()
                    scaleX = 1f - eased.absoluteValue
                    scaleY = scaleX
                    shadowElevation = if (eased != 0f) 6.dp.toPx() else 0f
                    transformOrigin = TransformOrigin(
                        pivotFractionX = if (eased > 0) 1f else 0f,
                        pivotFractionY = 0.5f,
                    )
                    clip = eased != 0f

                    // TODO: interpolate from rectangle to shape?
                    this.shape = if (eased != 0f) shape else RectangleShape
                },
        ) {
            val values = providedValues[topContentProvider.backStackRecord]?.provideValues()
            val providedLocals = remember(values) {
                values?.toTypedArray() ?: emptyArray()
            }
            CompositionLocalProvider(*providedLocals) { topContentProvider.content() }
        }
    }
}
