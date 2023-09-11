// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.store

import android.app.Application
import app.tivi.data.licenses.OpenSourceItem
import app.tivi.data.licenses.OpenSourceState
import app.tivi.data.licenses.SimpleOpenSourceState
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidOpenSourceStore(private val context: Application) : OpenSourceStore {
    private var lastAuthState: OpenSourceState = OpenSourceState.Empty

    override fun fetch(): OpenSourceState {
        return SimpleOpenSourceState(getOpenSourceItemList())
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun getOpenSourceItemList(): Flow<List<OpenSourceItem>> {
        try {
            val inputStream: InputStream = context.assets.open("generated_licenses.json")
            val json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
            return inputStream.bufferedReader().lineSequence().map { json.decodeFromString<List<OpenSourceItem>>(it).toMutableList() }.asFlow()
        } catch (ex: IOException) {
            return emptyFlow()
        }
    }

    override fun save(state: OpenSourceState) {
        lastAuthState = state
    }

    override fun clear() {
        lastAuthState = OpenSourceState.Empty
    }
}
