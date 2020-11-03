/*
 * Copyright 2020 Google LLC
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

package app.tivi.common.compose

import androidx.compose.foundation.Text
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.tivi.data.entities.SortOption

@Composable
fun SortMenuPopup(
    options: List<SortOption>,
    onSortSelected: (SortOption) -> Unit,
    icon: @Composable () -> Unit,
    iconModifier: Modifier = Modifier,
) {
    var sortPopupOpen by remember { mutableStateOf(false) }
    DropdownMenu(
        toggle = {
            IconButton(
                onClick = { sortPopupOpen = true },
                icon = { icon() }
            )
        },
        toggleModifier = iconModifier,
        expanded = sortPopupOpen,
        onDismissRequest = { sortPopupOpen = false },
    ) {
        options.forEach { sort ->
            DropdownMenuItem(
                onClick = {
                    onSortSelected(sort)
                    // Dismiss the popup
                    sortPopupOpen = false
                }
            ) {
                Text(
                    text = when (sort) {
                        SortOption.SUPER_SORT -> stringResource(R.string.popup_sort_super)
                        SortOption.ALPHABETICAL -> stringResource(R.string.popup_sort_alpha)
                        SortOption.LAST_WATCHED -> stringResource(R.string.popup_sort_last_watched)
                        SortOption.DATE_ADDED -> stringResource(R.string.popup_sort_date_followed)
                    }
                )
            }
        }
    }
}
