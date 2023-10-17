// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
import app.tivi.common.compose.LocalStrings
import app.tivi.data.models.SortOption

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
          text = currentSortOption.label(LocalStrings.current),
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
