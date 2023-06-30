// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.runtime.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Screen

/**
 * Represents a composable UI for the given [UiState]. Conventionally, this should just be the
 * return type of a ui function and a thin shim over a "real" implementation.
 *
 * This has two main benefits:
 * 1. Discouraging properties and general non-composable state that writing a class may invite.
 * 2. Ensuring separation of [Ui] instance from [Screen] specific ui composables allows for and
 *    encourages easy UI previews via Compose's `@Preview` annotations.
 *
 * Usage:
 * ```
 * internal fun tacoUi(): Ui<State> = ui { state, modifier ->
 *   Tacos(state, modifier)
 * }
 *
 * @Composable private fun Tacos(state: State, modifier: Modifier = Modifier) {...}
 *
 * @Preview
 * @Composable
 * private fun PreviewTacos() = Tacos(...)
 * ```
 *
 * This could be a class, but isn't necessary unless you're using dependency injection. Most UIs
 * don't use dependency injection at all however, unless maybe getting assisted injections of things
 * like image loaders.
 *
 * If a given `Presenter` only ever emits the same state, you can define a single value-less
 * `object` type for the state.
 *
 * Note that due to a bug in studio, we can't make this a `fun interface` _yet_. Instead, use [ui].
 *
 * @see ui
 */
public interface Ui<UiState : CircuitUiState> {
  @Composable public fun Content(state: UiState, modifier: Modifier)

  /**
   * A factory that creates [Ui's][Ui] for a given [Screen].
   *
   * Note that individual UIs should just be top-level [ui] function calls that factories simply
   * call into. This allows easily standing up composable preview functions.
   *
   * ```
   * class FavoritesUiFactory @Inject constructor() : Ui.Factory {
   *  override fun create(
   *    screen: Screen,
   *    context: CircuitContext
   *  ): Ui<*>? {
   *    return when (screen) {
   *      is AddFavorites -> addFavoritesUi()
   *      else -> null
   *    }
   *   }
   * }
   *
   * private fun addFavoritesUi() =
   *   ui<AddFavorites.State> { state, modifier -> Favorites(state, modifier) }
   *
   * @Composable private fun Favorites(state: State, modifier: Modifier = Modifier) {...}
   * ```
   */
  public fun interface Factory {
    public fun create(screen: Screen, context: CircuitContext): Ui<*>?
  }
}

/**
 * Due to this bug in Studio, we can't write lambda impls of [Ui] directly. This works around it by
 * offering a shim function of the same name. Once it's fixed, we can remove this and make [Ui] a
 * fun interface instead.
 *
 * Bug: https://issuetracker.google.com/issues/240292828
 *
 * @see [Ui] for main docs.
 */
public inline fun <UiState : CircuitUiState> ui(
  crossinline body: @Composable (state: UiState, modifier: Modifier) -> Unit
): Ui<UiState> {
  return object : Ui<UiState> {
    @Composable
    override fun Content(state: UiState, modifier: Modifier) {
      body(state, modifier)
    }
  }
}
