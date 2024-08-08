// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.tivi.common.ui.resources.strings.Res
import app.tivi.common.ui.resources.strings.popupSortAirDate
import app.tivi.common.ui.resources.strings.popupSortAlpha
import app.tivi.common.ui.resources.strings.popupSortDateFollowed
import app.tivi.common.ui.resources.strings.popupSortLastWatched
import app.tivi.data.models.SortOption
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
          text = stringResource(sort.label),
          fontWeight = if (sort == currentSortOption) FontWeight.Bold else null,
        )
      },
      onClick = { onItemClick(sort) },
      modifier = modifier,
    )
  }
}

internal val SortOption.label: StringResource get() = when (this) {
  SortOption.ALPHABETICAL -> Res.string.popupSortAlpha
  SortOption.LAST_WATCHED -> Res.string.popupSortLastWatched
  SortOption.DATE_ADDED -> Res.string.popupSortDateFollowed
  SortOption.AIR_DATE -> Res.string.popupSortAirDate
}
