// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.runtime

import androidx.compose.runtime.Immutable

/**
 * Marker interface for all UiEvent types.
 *
 * Events in Circuit should generally reflect user interactions with the UI. They are mediated by a
 * `Presenter` and may or may not influence the current [state][CircuitUiState].
 *
 * **Circuit event types are annotated as [@Immutable][Immutable] and should only use immutable
 * properties.**
 */
@Immutable public interface CircuitUiEvent
