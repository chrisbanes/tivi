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

import android.app.Application
import app.tivi.data.traktauth.AppAuthAuthStateWrapper
import app.tivi.data.traktauth.AuthState
import com.google.android.gms.auth.blockstore.Blockstore
import com.google.android.gms.auth.blockstore.BlockstoreClient
import com.google.android.gms.auth.blockstore.RetrieveBytesRequest
import com.google.android.gms.auth.blockstore.StoreBytesData
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.tasks.await
import me.tatarka.inject.annotations.Inject

@Inject
class BlockStoreAuthStore(
    private val context: Application,
) : AuthStore {
    private val client by lazy { Blockstore.getClient(context) }

    private val playServicesAvailable: Boolean
        get() = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    override suspend fun get(): AuthState? = runCatching {
        // For new clients, we store using our app's key
        // The old version of the BlockStore used an implicit key,
        // so we need to check for that too
        get(TRAKT_AUTH_KEY) ?: get(BlockstoreClient.DEFAULT_BYTES_DATA_KEY)?.also {
            // Clear out any saved state, and save it to our app's key
            clear()
            save(it)
        }
    }.getOrNull()

    override suspend fun save(state: AuthState) {
        save(TRAKT_AUTH_KEY, state.serializeToJson().encodeToByteArray())
    }

    override suspend fun clear() {
        val zero = ByteArray(0)
        save(TRAKT_AUTH_KEY, zero)
        save(BlockstoreClient.DEFAULT_BYTES_DATA_KEY, zero)
    }

    private suspend fun get(key: String): AuthState? {
        val response = client.retrieveBytes(
            RetrieveBytesRequest.Builder()
                .setKeys(listOf(key))
                .build(),
        ).await()

        return response.blockstoreDataMap[key]
            ?.bytes
            ?.decodeToString()
            ?.let(::AppAuthAuthStateWrapper)
    }

    private suspend fun save(key: String, bytes: ByteArray) {
        val data = StoreBytesData.Builder()
            .setShouldBackupToCloud(false)
            .setKey(key)
            .setBytes(bytes)
            .build()

        client.storeBytes(data).await()
    }

    override suspend fun isAvailable(): Boolean = playServicesAvailable

    private companion object {
        const val TRAKT_AUTH_KEY = "app.tivi.blockstore.trakt_auth"
    }
}
