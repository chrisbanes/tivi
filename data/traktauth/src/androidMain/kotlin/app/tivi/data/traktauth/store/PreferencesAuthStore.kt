/*
 * Copyright 2022 Google LLC
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

package app.tivi.data.traktauth.store

import android.content.SharedPreferences
import androidx.core.content.edit
import app.tivi.data.traktauth.AppAuthAuthStateWrapper
import app.tivi.data.traktauth.AuthState
import me.tatarka.inject.annotations.Inject

typealias AuthSharedPreferences = SharedPreferences

@Inject
class PreferencesAuthStore(
    private val authPrefs: Lazy<AuthSharedPreferences>,
) : AuthStore {
    override suspend fun get(): AuthState? {
        return authPrefs.value
            .getString(PreferenceAuthKey, null)
            ?.let(::AppAuthAuthStateWrapper)
    }

    override suspend fun save(state: AuthState) {
        authPrefs.value.edit(commit = true) {
            putString(PreferenceAuthKey, state.serializeToJson())
        }
    }

    override suspend fun clear() {
        authPrefs.value.edit(commit = true) {
            remove(PreferenceAuthKey)
        }
    }

    private companion object {
        private const val PreferenceAuthKey = "stateJson"
    }
}
