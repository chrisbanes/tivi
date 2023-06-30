// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.foundation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.NavDecoration
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.providedValuesForBackStack
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen

@Composable
public fun NavigableCircuitContent(
  navigator: Navigator,
  backstack: SaveableBackStack,
  modifier: Modifier = Modifier,
  circuitConfig: CircuitConfig = requireNotNull(LocalCircuitConfig.current),
  providedValues: Map<out BackStack.Record, ProvidedValues> = providedValuesForBackStack(backstack),
  decoration: NavDecoration = circuitConfig.defaultNavDecoration,
  unavailableRoute: (@Composable (screen: Screen, modifier: Modifier) -> Unit) =
    circuitConfig.onUnavailableContent,
) {
  val activeContentProviders = buildList {
    for (record in backstack) {
      val provider =
        key(record.key) {
          val screen = record.screen

          val currentContent: (@Composable (SaveableBackStack.Record) -> Unit) = {
            CircuitContent(screen, modifier, navigator, circuitConfig, unavailableRoute)
          }

          val currentRouteContent by rememberUpdatedState(currentContent)
          val currentRecord by rememberUpdatedState(record)
          remember { movableContentOf { currentRouteContent(currentRecord) } }
        }
      add(record to provider)
    }
  }

  if (backstack.size > 0) {
    @Suppress("SpreadOperator")
    decoration.DecoratedContent(activeContentProviders.first(), backstack.size, modifier) {
      (record, provider) ->
      val values = providedValues[record]?.provideValues()
      val providedLocals = remember(values) { values?.toTypedArray() ?: emptyArray() }
      CompositionLocalProvider(*providedLocals) { provider() }
    }
  }
}

/** Default values and common alternatives used by navigable composables. */
public object NavigatorDefaults {

  private const val FIVE_PERCENT = 0.05f
  private val SlightlyRight = { width: Int -> (width * FIVE_PERCENT).toInt() }
  private val SlightlyLeft = { width: Int -> 0 - (width * FIVE_PERCENT).toInt() }

  /** The default [NavDecoration] used in navigation. */
  public object DefaultDecoration : NavDecoration {
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun <T> DecoratedContent(
      arg: T,
      backStackDepth: Int,
      modifier: Modifier,
      content: @Composable (T) -> Unit
    ) {
      // Remember the previous stack depth so we know if the navigation is going "back".
      val prevStackDepth = rememberSaveable { mutableStateOf(backStackDepth) }
      val diff = backStackDepth - prevStackDepth.value
      prevStackDepth.value = backStackDepth
      AnimatedContent(
        targetState = arg,
        modifier = modifier,
        transitionSpec = {
          // Mirror the forward and backward transitions of activities in Android 33
          if (diff > 0) {
            slideInHorizontally(tween(), SlightlyRight) + fadeIn() with
              slideOutHorizontally(tween(), SlightlyLeft) + fadeOut()
          } else
            if (diff < 0) {
                slideInHorizontally(tween(), SlightlyLeft) + fadeIn() with
                  slideOutHorizontally(tween(), SlightlyRight) + fadeOut()
              } else {
                // Crossfade if there was no diff
                fadeIn() with fadeOut()
              }
              .using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
              )
        },
      ) {
        content(it)
      }
    }
  }

  /** An empty [NavDecoration] that emits the content with no surrounding decoration or logic. */
  public object EmptyDecoration : NavDecoration {
    @Composable
    override fun <T> DecoratedContent(
      arg: T,
      backStackDepth: Int,
      modifier: Modifier,
      content: @Composable (T) -> Unit
    ) {
      content(arg)
    }
  }
}
