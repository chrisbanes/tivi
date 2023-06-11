// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.tivi.common.ui.resources.MR
import app.tivi.data.models.SortOption
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource

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

internal val SortOption.labelResId: StringResource
    get() = when (this) {
        SortOption.ALPHABETICAL -> MR.strings.popup_sort_alpha
        SortOption.LAST_WATCHED -> MR.strings.popup_sort_last_watched
        SortOption.DATE_ADDED -> MR.strings.popup_sort_date_followed
        SortOption.AIR_DATE -> MR.strings.popup_sort_air_date
    }
