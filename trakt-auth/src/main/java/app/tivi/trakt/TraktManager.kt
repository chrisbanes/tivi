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
import app.tivi.inject.ProcessLifetime
import app.tivi.util.AppCoroutineDispatchers
import com.uwetrottmann.trakt5.TraktV2
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
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
    private val traktClient: Lazy<TraktV2>,
    @ProcessLifetime val processScope: CoroutineScope
) {
    private val authState = ConflatedBroadcastChannel<AuthState>()

    private val _state = ConflatedBroadcastChannel(TraktAuthState.LOGGED_OUT)
    val state: Flow<TraktAuthState>
        get() = _state.asFlow()

    init {
        // Observer which updates local state
        processScope.launch {
            authState.asFlow().collect { authState ->
                updateAuthState(authState)

                traktClient.get().apply {
                    accessToken(authState.accessToken)
                    refreshToken(authState.refreshToken)
                }
            }
        }

        // Read the auth state from prefs
        processScope.launch {
            val state = withContext(dispatchers.io) { readAuthState() }
            authState.send(state)
        }
    }

    private suspend fun updateAuthState(authState: AuthState) {
        if (authState.isAuthorized) {
            _state.send(TraktAuthState.LOGGED_IN)
        } else {
            _state.send(TraktAuthState.LOGGED_OUT)
        }
    }

    suspend fun clearAuth() {
        authState.send(AuthState())
        clearPersistedAuthState()
    }

    fun onNewAuthState(newState: AuthState) {
        processScope.launch(dispatchers.main) {
            // Update our local state
            authState.send(newState)
        }
        processScope.launch(dispatchers.io) {
            // Persist auth state
            persistAuthState(newState)
        }
        // Now trigger a sync of all shows
        showTasks.syncFollowedShowsWhenIdle()
    }

    private fun readAuthState(): AuthState {
        val stateJson = authPrefs.getString("stateJson", null)
        return when {
            stateJson != null -> AuthState.jsonDeserialize(stateJson)
            else -> AuthState()
        }
    }

    private fun persistAuthState(state: AuthState) {
        authPrefs.edit(commit = true) {
            putString("stateJson", state.jsonSerializeString())
        }
    }

    private fun clearPersistedAuthState() {
        authPrefs.edit(commit = true) {
            remove("stateJson")
        }
    }
}
