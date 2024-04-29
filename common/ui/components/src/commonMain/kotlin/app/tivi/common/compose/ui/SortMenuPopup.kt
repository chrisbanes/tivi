// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.tivi.common.compose.LocalStrings
import app.tivi.common.ui.resources.TiviStrings
import app.tivi.data.models.SortOption

@Composable
internal fun ColumnScope.SortDropdownMenuContent(
  sortOptions: List<SortOption>,
  onItemClick: (SortOption) -> Unit,
  modifier: Modifier = Modifier,
  currentSortOption: SortOption? = null,
) {
  for (sort in sortOptions) {
    val strings = LocalStrings.current
    DropdownMenuItem(
      text = {
        Text(
          text = sort.label(strings),
          fontWeight = if (sort == currentSortOption) FontWeight.Bold else null,
        )
      },
      onClick = { onItemClick(sort) },
      modifier = modifier,
    )
  }
}

internal fun SortOption.label(strings: TiviStrings): String = when (this) {
  SortOption.ALPHABETICAL -> strings.popupSortAlpha
  SortOption.LAST_WATCHED -> strings.popupSortLastWatched
  SortOption.DATE_ADDED -> strings.popupSortDateFollowed
  SortOption.AIR_DATE -> strings.popupSortAirDate
}
