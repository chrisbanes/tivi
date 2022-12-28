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

package app.tivi.trakt.store

import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Named
import net.openid.appauth.AuthState

class PreferencesAuthStore @Inject constructor(
    @Named("auth") private val authPrefs: Lazy<SharedPreferences>,
) : AuthStore {
    override suspend fun get(): AuthState? {
        return authPrefs.get()
            .getString(PreferenceAuthKey, null)
            ?.let(AuthState::jsonDeserialize)
    }

    override suspend fun save(state: AuthState) {
        authPrefs.get().edit(commit = true) {
            putString(PreferenceAuthKey, state.jsonSerializeString())
        }
    }

    override suspend fun clear() {
        authPrefs.get().edit(commit = true) {
            remove(PreferenceAuthKey)
        }
    }

    private companion object {
        private const val PreferenceAuthKey = "stateJson"
    }
}
