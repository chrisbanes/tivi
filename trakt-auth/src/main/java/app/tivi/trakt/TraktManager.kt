/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.trakt

import android.content.SharedPreferences
import androidx.core.content.edit
import app.tivi.actions.ShowTasks
import app.tivi.util.AppCoroutineDispatchers
import com.uwetrottmann.trakt5.TraktV2
import dagger.Lazy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TraktManager @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    @Named("auth") private val authPrefs: SharedPreferences,
    private val showTasks: ShowTasks,
    private val traktClient: Lazy<TraktV2>
) {
    private val authState = MutableStateFlow(EmptyAuthState)

    private val _state = MutableStateFlow(TraktAuthState.LOGGED_OUT)
    val state: Flow<TraktAuthState>
        get() = _state

    init {
        // Observer which updates local state
        GlobalScope.launch(dispatchers.io) {
            authState.collect { authState ->
                updateAuthState(authState)

                traktClient.get().apply {
                    accessToken(authState.accessToken)
                    refreshToken(authState.refreshToken)
                }
            }
        }

        // Read the auth state from prefs
        GlobalScope.launch(dispatchers.main) {
            val state = withContext(dispatchers.io) { readAuthState() }
            authState.value = state
        }

        // Read the auth state from prefs
        GlobalScope.launch(dispatchers.main) {
            val state = withContext(dispatchers.io) { readAuthState() }
            authState.value = state
        }
    }

    private fun updateAuthState(authState: AuthState) {
        if (authState.isAuthorized) {
            _state.value = TraktAuthState.LOGGED_IN
        } else {
            _state.value = TraktAuthState.LOGGED_OUT
        }
    }

    fun clearAuth() {
        authState.value = EmptyAuthState
        clearPersistedAuthState()
    }

    fun onNewAuthState(newState: AuthState) {
        GlobalScope.launch(dispatchers.main) {
            // Update our local state
            authState.value = newState
        }
        GlobalScope.launch(dispatchers.io) {
            // Persist auth state
            persistAuthState(newState)
        }
        // Now trigger a sync of all shows
        showTasks.syncFollowedShowsWhenIdle()
    }

    private fun readAuthState(): AuthState {
        val stateJson = authPrefs.getString(PreferenceAuthKey, null)
        return when {
            stateJson != null -> AuthState.jsonDeserialize(stateJson)
            else -> AuthState()
        }
    }

    private fun persistAuthState(state: AuthState) {
        authPrefs.edit(commit = true) {
            putString(PreferenceAuthKey, state.jsonSerializeString())
        }
    }

    private fun clearPersistedAuthState() {
        authPrefs.edit(commit = true) {
            remove(PreferenceAuthKey)
        }
    }

    companion object {
        private val EmptyAuthState = AuthState()
        private const val PreferenceAuthKey = "stateJson"
    }
}
