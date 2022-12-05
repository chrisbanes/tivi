/*
 * Copyright 2021 Google LLC
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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.entities.SortOption

@Composable
internal fun ColumnScope.SortDropdownMenuContent(
    sortOptions: List<SortOption>,
    onItemClick: (SortOption) -> Unit,
    modifier: Modifier = Modifier,
    currentSortOption: SortOption? = null,
) {
    for (sort in sortOptions) {
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(sort.labelResId),
                    fontWeight = if (sort == currentSortOption) FontWeight.Bold else null,
                )
            },
            onClick = { onItemClick(sort) },
            modifier = modifier,
        )
    }
}

internal val SortOption.labelResId: Int
    @StringRes get() = when (this) {
        SortOption.SUPER_SORT -> UiR.string.popup_sort_super
        SortOption.ALPHABETICAL -> UiR.string.popup_sort_alpha
        SortOption.LAST_WATCHED -> UiR.string.popup_sort_last_watched
        SortOption.DATE_ADDED -> UiR.string.popup_sort_date_followed
    }
