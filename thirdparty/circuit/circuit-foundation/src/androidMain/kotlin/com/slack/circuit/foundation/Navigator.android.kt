// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.foundation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.runtime.Navigator

/**
 * Returns a new [Navigator] for navigating within [CircuitContents][CircuitContent]. Delegates
 * onRootPop to the [LocalOnBackPressedDispatcherOwner].
 *
 * @param backstack The backing [SaveableBackStack] to navigate.
 * @param enableBackHandler Indicates whether or not [Navigator.pop] should be called by the system
 *   back handler. Defaults to true.
 * @see NavigableCircuitContent
 */
@Composable
public fun rememberCircuitNavigator(
  backstack: SaveableBackStack,
  enableBackHandler: Boolean = true,
): Navigator {
  val navigator =
    rememberCircuitNavigator(
      backstack = backstack,
      onRootPop = backDispatcherRootPop(),
    )
  BackHandler(enabled = enableBackHandler && backstack.size > 1, onBack = navigator::pop)

  return navigator
}

@Composable
private fun backDispatcherRootPop(): () -> Unit {
  val onBackPressedDispatcher =
    LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
      ?: error("No OnBackPressedDispatcherOwner found, unable to handle root navigation pops.")
  return onBackPressedDispatcher::onBackPressed
}
