// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.opensource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable

interface OpenSourceState {
    val openSourceList: Flow<List<OpenSourceItem>>
    fun serializeToJson(): String

    companion object {
        val Empty: OpenSourceState = object : OpenSourceState {
            override val openSourceList: Flow<List<OpenSourceItem>> = emptyFlow()
            override fun serializeToJson(): String = "{}"
        }
    }
}

data class SimpleOpenSourceState(
    override val openSourceList: Flow<List<OpenSourceItem>>,
) : OpenSourceState {
    override fun serializeToJson(): String = openSourceList.toString()
}


@Serializable
data class OpenSourceItem(
    val first: String,
    val second: String,
)
