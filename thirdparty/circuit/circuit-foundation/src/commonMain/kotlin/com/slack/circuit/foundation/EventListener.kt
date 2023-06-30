// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.foundation

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui

/**
 * A listener for tracking the state changes of a given [Screen]. This can be used for
 * instrumentation and other telemetry.
 */
public interface EventListener {

  /** Called just before creating a [Presenter] for a given [screen]. */
  public fun onBeforeCreatePresenter(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext
  ) {}

  /**
   * Called just after creating a [presenter] for a given [screen]. The presenter may be null if
   * none was found.
   */
  public fun onAfterCreatePresenter(
    screen: Screen,
    navigator: Navigator,
    presenter: Presenter<*>?,
    context: CircuitContext
  ) {}

  /** Called just before creating a [Ui] for a given [screen]. */
  public fun onBeforeCreateUi(screen: Screen, context: CircuitContext) {}

  /**
   * Called just after creating a [ui] for a given [screen]. The ui may be null if none was found.
   */
  public fun onAfterCreateUi(screen: Screen, ui: Ui<*>?, context: CircuitContext) {}

  /** Called when no content was found and one or both of [presenter] and [ui] are null. */
  public fun onUnavailableContent(
    screen: Screen,
    presenter: Presenter<*>?,
    ui: Ui<*>?,
    context: CircuitContext
  ) {}

  /**
   * Called when there is a new [state] returned by the [Presenter]. This is called every time state
   * is recomposed.
   */
  public fun onState(state: CircuitUiState) {}

  /** Called once before the initial [Presenter.present] call. */
  public fun onStartPresent() {}

  /** Called once after the [Presenter.present] composition is disposed. */
  public fun onDisposePresent() {}

  /** Called once before the initial [Ui.Content] call. */
  public fun onStartContent() {}

  /** Called once after the [Ui.Content] composition is disposed. */
  public fun onDisposeContent() {}

  /** Called once before doing any processing for the requested circuit. */
  public fun start() {}

  /**
   * Called once when this [EventListener] should be disposed and the corresponding circuit is
   * disposed.
   */
  public fun dispose() {}

  public fun interface Factory {
    public fun create(screen: Screen, context: CircuitContext): EventListener
  }

  public companion object {
    public val NONE: EventListener = object : EventListener {}
  }
}
