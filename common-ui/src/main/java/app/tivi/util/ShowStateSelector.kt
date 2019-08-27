/*
 * Copyright 2019 Google LLC
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

package app.tivi.util

import app.tivi.data.entities.TiviShow
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow

class ShowStateSelector {
    private var selectedShowIds: Set<Long> = emptySet()
    private val selectedShowIdsChannel = ConflatedBroadcastChannel(selectedShowIds)

    private var isSelectionOpen = false
    private val isSelectionOpenChannel = ConflatedBroadcastChannel(isSelectionOpen)

    fun observeSelectedShowIds() = selectedShowIdsChannel.asFlow()

    fun observeIsSelectionOpen() = isSelectionOpenChannel.asFlow()

    fun getSelectedShowIds() = selectedShowIdsChannel.value

    fun onItemClick(show: TiviShow): Boolean {
        if (isSelectionOpen) {
            val currentSelection = when {
                show.id in selectedShowIds -> selectedShowIds.minus(show.id)
                else -> selectedShowIds.plus(show.id)
            }
            isSelectionOpen = currentSelection.isNotEmpty()
            selectedShowIds = currentSelection
            dispatch()
            return true
        }
        return false
    }

    fun onItemLongClick(show: TiviShow): Boolean {
        if (!isSelectionOpen) {
            isSelectionOpen = true

            var currentSelection = selectedShowIds
            if (show.id !in currentSelection) {
                currentSelection = currentSelection.plus(show.id)
            }

            isSelectionOpen = currentSelection.isNotEmpty()
            selectedShowIds = currentSelection
            dispatch()
            return true
        }
        return false
    }

    fun clearSelection() {
        selectedShowIds = emptySet()
        isSelectionOpen = false
        dispatch()
    }

    private fun dispatch() {
        selectedShowIdsChannel.sendBlocking(selectedShowIds)
        isSelectionOpenChannel.sendBlocking(isSelectionOpen)
    }
}