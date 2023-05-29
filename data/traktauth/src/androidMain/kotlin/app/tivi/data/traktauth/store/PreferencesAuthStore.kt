// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
