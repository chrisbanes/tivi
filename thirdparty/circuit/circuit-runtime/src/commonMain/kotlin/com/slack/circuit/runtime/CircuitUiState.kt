// Copyright (C) 2023 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.runtime

import androidx.compose.runtime.Stable

/**
 * Marker interface for all UiState types.
 *
 * States in Circuit should be minimal data models that a `Ui` can render. They are produced by a
 * `Presenter` that interpret the underlying data layer and mediate input user/nav
 * [events][CircuitUiEvent].
 *
 * `Ui`s receive state as a parameter and should act as pure functions that render the input state
 * as a UI. They should not have any side effects or directly interact with the underlying data
 * layer.
 *
 * **Circuit state types are annotated as [@Stable][Stable] and should only use stable properties.**
 */
@Stable public interface CircuitUiState
