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

package app.tivi.ui

import androidx.appcompat.widget.PopupMenu
import androidx.core.view.get
import app.tivi.common.ui.R
import app.tivi.data.entities.SortOption
import app.tivi.ui.widget.PopupMenuButton

class SortPopupMenuListener(
    private val selectedSort: SortOption,
    private val availableSorts: List<SortOption>
) : PopupMenuButton.PopupMenuListener {
    override fun onPreparePopupMenu(popupMenu: PopupMenu) {
        val menu = popupMenu.menu
        for (index in 0 until menu.size()) {
            val menuItem = menu[index]
            val sortOption = popupMenuItemIdToSortOption(menuItem.itemId) ?: break

            menuItem.isVisible = availableSorts.contains(sortOption)
            if (selectedSort == sortOption) {
                menuItem.isChecked = true
            }
        }
    }
}

fun popupMenuItemIdToSortOption(itemId: Int) = when (itemId) {
    R.id.popup_sort_super -> SortOption.SUPER_SORT
    R.id.popup_sort_date_followed -> SortOption.DATE_ADDED
    R.id.popup_sort_alpha -> SortOption.ALPHABETICAL
    R.id.popup_sort_last_watched -> SortOption.LAST_WATCHED
    else -> null
}
