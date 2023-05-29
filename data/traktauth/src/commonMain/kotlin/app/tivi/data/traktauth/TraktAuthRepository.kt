// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.tivi.data.traktauth.store.AuthStore
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@OptIn(DelicateCoroutinesApi::class)
@ApplicationScope
@Inject
class TraktAuthRepository(
    private val dispatchers: AppCoroutineDispatchers,
    private val authStore: AuthStore,
) {
    private val authState = MutableStateFlow(AuthState.Empty)

    private val _state = MutableStateFlow(TraktAuthState.LOGGED_OUT)
    val state: StateFlow<TraktAuthState> get() = _state.asStateFlow()

    init {
        // Observer which updates local state
        GlobalScope.launch(dispatchers.main) {
            authState.collect { authState ->
                updateAuthState(authState)
            }
        }

        // Read the auth state from prefs
        GlobalScope.launch(dispatchers.main) {
            val state = withContext(dispatchers.io) { authStore.get() }
            authState.value = state ?: AuthState.Empty
        }
    }

    private fun updateAuthState(authState: AuthState) {
        _state.value = when {
            authState.isAuthorized -> TraktAuthState.LOGGED_IN
            else -> TraktAuthState.LOGGED_OUT
        }
    }

    fun clearAuth() {
        authState.value = AuthState.Empty
        GlobalScope.launch(dispatchers.io) { authStore.clear() }
    }

    fun onNewAuthState(newState: AuthState) {
        // Update our local state
        authState.value = newState
        updateAuthState(newState)

        GlobalScope.launch(dispatchers.io) {
            // Persist auth state
            authStore.save(newState)
        }
    }
}
