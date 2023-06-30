// Copyright (C) 2023 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.foundation

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter

/**
 * A Circuit call back to help navigate to different screens. Intended to be used when forwarding
 * [NavEvent]s from nested [Presenter]s.
 */
public fun Navigator.onNavEvent(event: NavEvent) {
  when (event) {
    NavEvent.Pop -> pop()
    is NavEvent.GoTo -> goTo(event.screen)
    is NavEvent.ResetRoot -> resetRoot(event.newRoot)
  }
}

/** A sealed navigation interface intended to be used when making a navigation callback. */
public sealed interface NavEvent : CircuitUiEvent {
  /** Corresponds to [Navigator.pop]. */
  public object Pop : NavEvent

  /** Corresponds to [Navigator.goTo]. */
  public data class GoTo(internal val screen: Screen) : NavEvent

  /** Corresponds to [Navigator.resetRoot]. */
  public data class ResetRoot(internal val newRoot: Screen) : NavEvent
}
