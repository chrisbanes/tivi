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
import kotlinx.coroutines.flow.MutableStateFlow

class ShowStateSelector {
    private val selectedShowIds = MutableStateFlow<Set<Long>>(emptySet())
    private val isSelectionOpen = MutableStateFlow(false)

    fun observeSelectedShowIds() = selectedShowIds

    fun observeIsSelectionOpen() = isSelectionOpen

    fun getSelectedShowIds() = selectedShowIds.value

    fun onItemClick(show: TiviShow): Boolean {
        if (isSelectionOpen.value) {
            val selectedIds = selectedShowIds.value
            val newSelection = when (show.id) {
                in selectedIds -> selectedIds - show.id
                else -> selectedIds + show.id
            }
            isSelectionOpen.value = newSelection.isNotEmpty()
            selectedShowIds.value = newSelection
            return true
        }
        return false
    }

    fun onItemLongClick(show: TiviShow): Boolean {
        if (!isSelectionOpen.value) {
            isSelectionOpen.value = true

            val newSelection = selectedShowIds.value + show.id
            isSelectionOpen.value = newSelection.isNotEmpty()
            selectedShowIds.value = newSelection
            return true
        }
        return false
    }

    fun clearSelection() {
        selectedShowIds.value = emptySet()
        isSelectionOpen.value = false
    }
}
