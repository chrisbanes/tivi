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

package app.tivi.common.compose.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tivi.data.models.SortOption
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortChip(
    sortOptions: List<SortOption>,
    currentSortOption: SortOption,
    modifier: Modifier = Modifier,
    onSortSelected: (SortOption) -> Unit,
) {
    Box(modifier) {
        var expanded by remember { mutableStateOf(false) }

        FilterChip(
            selected = true,
            onClick = { expanded = true },
            label = {
                Text(
                    text = stringResource(currentSortOption.labelResId),
                    modifier = Modifier.animateContentSize(),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "",
                    modifier = Modifier.size(16.dp),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null, // decorative
                    modifier = Modifier.size(16.dp),
                )
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SortDropdownMenuContent(
                sortOptions = sortOptions,
                currentSortOption = currentSortOption,
                onItemClick = {
                    onSortSelected(it)
                    expanded = false
                },
            )
        }
    }
}
