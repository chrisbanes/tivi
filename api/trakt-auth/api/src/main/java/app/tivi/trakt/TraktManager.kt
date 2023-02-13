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

import app.tivi.inject.ApplicationScope
import app.tivi.tasks.ShowTasks
import app.tivi.trakt.store.AuthStore
import app.tivi.util.AppCoroutineDispatchers
import com.uwetrottmann.trakt5.TraktV2
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
class TraktManager(
    private val dispatchers: AppCoroutineDispatchers,
    private val showTasks: ShowTasks,
    private val traktClient: Lazy<TraktV2>,
    private val authStore: AuthStore,
) {
    private val authState = MutableStateFlow(AuthState.Empty)

    private val _state = MutableStateFlow(TraktAuthState.LOGGED_OUT)
    val state: StateFlow<TraktAuthState>
        get() = _state.asStateFlow()

    init {
        // Observer which updates local state
        GlobalScope.launch(dispatchers.io) {
            authState.collect { authState ->
                updateAuthState(authState)

                traktClient.value.apply {
                    accessToken(authState.accessToken)
                    refreshToken(authState.refreshToken)
                }
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
        GlobalScope.launch(dispatchers.main) {
            // Update our local state
            authState.value = newState
        }
        GlobalScope.launch(dispatchers.io) {
            // Persist auth state
            authStore.save(newState)
        }
        // Now trigger a sync of all shows
        showTasks.syncWatchedShows()
        showTasks.syncFollowedShows()
    }
}
