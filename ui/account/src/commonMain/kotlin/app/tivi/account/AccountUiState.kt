// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.account

import androidx.compose.runtime.Immutable
import app.tivi.data.models.TraktUser
import app.tivi.data.traktauth.TraktAuthState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class AccountUiState(
    val user: TraktUser? = null,
    val authState: TraktAuthState = TraktAuthState.LOGGED_OUT,
    val eventSink: (AccountUiEvent) -> Unit,
) : CircuitUiState

sealed interface AccountUiEvent : CircuitUiEvent {
    object Login : AccountUiEvent
    object Logout : AccountUiEvent
    object NavigateToSettings : AccountUiEvent
}
