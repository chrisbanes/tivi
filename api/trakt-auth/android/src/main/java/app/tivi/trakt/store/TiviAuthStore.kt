/*
 * Copyright 2023 Google LLC
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

import app.tivi.trakt.AuthState
import me.tatarka.inject.annotations.Inject

@Inject
class TiviAuthStore(
    private val preferencesAuthStore: PreferencesAuthStore,
    private val blockStoreAuthStore: BlockStoreAuthStore,
) : AuthStore {
    override suspend fun get(): AuthState? {
        val prefResult: AuthState? = preferencesAuthStore.get()

        if (!blockStoreAuthStore.isAvailable()) {
            // Block Store isn't available, moving on...
            return prefResult
        }

        @Suppress("IfThenToElvis")
        return if (prefResult == null) {
            // If we don't have a pref result, try Block Store and save it to preferences
            blockStoreAuthStore.get()?.also { preferencesAuthStore.save(it) }
        } else {
            // If we have a pref result, save it to Block Store
            prefResult.also { blockStoreAuthStore.save(it) }
        }
    }

    override suspend fun save(state: AuthState) {
        preferencesAuthStore.save(state)

        if (blockStoreAuthStore.isAvailable()) {
            blockStoreAuthStore.save(state)
        }
    }

    override suspend fun clear() {
        preferencesAuthStore.clear()

        if (blockStoreAuthStore.isAvailable()) {
            blockStoreAuthStore.clear()
        }
    }
}
