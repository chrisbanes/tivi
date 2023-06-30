// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.runtime.presenter

import androidx.compose.runtime.Composable
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen

/**
 * Presents a given [UiState].
 *
 * Events (if any) should be a part of the [UiState] itself as an `eventSink: (Event) -> Unit`
 * property.
 *
 * If a given [Presenter] only ever emits the same state, you can define a single value-less
 * `object` type for the state.
 *
 * @see present for more thorough documentation.
 */
public interface Presenter<UiState : CircuitUiState> {
  /**
   * The primary [Composable] entry point to present a [UiState]. In production, a [Navigator] is
   * used to automatically connect this with a corresponding `Ui` to render the state returned by
   * this function.
   *
   * When handling events, embed a `eventSink: (Event) -> Unit` property in the state as needed.
   *
   * ```kotlin
   * data class State(
   *   val favorites: List<Favorite>,
   *   eventSink: (Event) -> Unit
   * ) : CircuitUiState
   *
   * class FavoritesPresenter(...) : Presenter<State, Event> {
   *   @Composable override fun present(): State {
   *     // ...
   *     return State(...) { event ->
   *       // Handle UI events here
   *     }
   *   }
   * }
   * ```
   *
   * ## Dependency Injection
   *
   * Presenters should use dependency injection, usually assisted injection to accept [Navigator] or
   * [Screen] instances as inputs. Their corresponding assisted factories should then be used by
   * hand-written [presenter factories][Presenter.Factory].
   *
   * ```kotlin
   * class FavoritesPresenter @AssistedInject constructor(
   *   @Assisted private val screen: FavoritesScreen,
   *   @Assisted private val navigator: Navigator,
   *   private val favoritesRepository: FavoritesRepository
   * ) : Presenter<State> {
   *   @Composable override fun present(): State {
   *     // ...
   *   }
   *
   *   @AssistedFactory
   *   fun interface Factory {
   *     fun create(screen: FavoritesScreen, navigator: Navigator): FavoritesPresenter
   *   }
   * }
   * ```
   *
   * ## Testing
   *
   * When testing, simply drive UI events with a `MutableSharedFlow` use Molecule+Turbine to drive
   * this function.
   *
   * ```
   * @Test
   * fun `emit initial state and refresh`() = runTest {
   *   val favorites = listOf("Moose", "Reeses", "Lola")
   *   val repository = FakeFavoritesRepository(favorites)
   *   val presenter = FavoritesPresenter(repository)
   *
   *   moleculeFlow(Immediate) { presenter.present() }
   *     .test {
   *       assertThat(awaitItem()).isEqualTo(State.Loading)
   *       val successState = awaitItem()
   *       assertThat(successState).isEqualTo(State.Success(favorites))
   *       successState.eventSink(Event.Refresh)
   *       assertThat(awaitItem()).isEqualTo(State.Success(favorites))
   *     }
   * }
   * ```
   *
   * Note that Circuit's test artifact has a `Presenter.test()` helper extension function for the
   * above case.
   */
  @Composable public fun present(): UiState

  /**
   * A factory that produces [presenters][Presenter] for a given [Screen]. `CircuitConfig` instances
   * use the created presenter and connects it to a given `Ui` for the same [Screen].
   *
   * Factories should be simple aggregate multiple presenters for a canonical "whole screen". That
   * is to say, they should be hand-written and aggregate all the presenters responsible for the UI
   * visible within the surface this presents on.
   *
   * ## Example
   *
   * Consider this example of a Profile UI.
   *
   * ```
   *                           ┌────────────────────┐
   *                      ┌─── │                    │
   *                      │    ├────────────────────┤◄──┐
   *                      │    │ X                  │   │
   *                      │    │                    │ ProfileHeaderPresenter
   *                      │    │ Fred Rogers        │   │
   *                      │    ├────────────────────┤◄──┘
   *                      │    │ ┌───────┐  ┌────┐  │
   * ProfilePresenterFactory   │ │Message│  │Call│◄─┼─── ProfileActionsPresenter
   *                      │    │ └───────┘  └────┘  │
   *                      │    │                    │
   *                      │    │  - - - - - - - - ◄─┼────┐
   *                      │    │  - - - - - - - -   │    │
   *                      │    │  - - - - - - - -   │  ProfileDetailsPresenter
   *                      │    │  - - - - - - - - ◄─┼────┘
   *                      └─── │                    │
   *                           └────────────────────┘
   * ```
   *
   * This would be represented by the following factory implementation:
   * ```kotlin
   * class ProfilePresenter.Factory @Inject constructor(
   *   val headerPresenter: ProfilerHeaderPresenter.Factory,
   *   val actionsPresenter: ProfilerActionsPresenter.Factory,
   *   val detailsPresenter: ProfilerDetailsPresenter.Factory,
   *   val callScreenRouter: CallScreenRouter.Factory
   * ) : Presenter.Factory {
   *   override fun create(screen: Screen, navigator: Navigator, context: CircuitContext): Presenter<*, *>? {
   *     return when (screen) {
   *       is ProfileHeader -> headerPresenter.create(screen)
   *       is ProfileActions -> actionsPresenter.create(screen, callScreenRouter.create(navigator))
   *       is ProfileDetails -> detailsPresenter.create(screen)
   *       else -> null
   *     }
   *   }
   * }
   * ```
   */
  // Diagram generated from asciiflow: https://shorturl.at/fgjtA
  public fun interface Factory {
    /**
     * Creates a [Presenter] for the given [screen] if it can handle it, or returns null if it
     * cannot handle the given [screen].
     */
    public fun create(screen: Screen, navigator: Navigator, context: CircuitContext): Presenter<*>?
  }
}

/**
 * Due to this bug in Studio, we can't write lambda impls of [Presenter] directly. This works around
 * it by offering a shim function of the same name. Once it's fixed, we can remove this and make
 * [Presenter] a fun interface instead.
 *
 * Bug: https://issuetracker.google.com/issues/240292828
 *
 * @see [Presenter] for main docs.
 */
public inline fun <UiState : CircuitUiState> presenterOf(
  crossinline body: @Composable () -> UiState
): Presenter<UiState> {
  return object : Presenter<UiState> {
    @Composable
    override fun present(): UiState {
      return body()
    }
  }
}
