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

package app.tivi.home.main

import android.view.Menu
import androidx.recyclerview.widget.DiffUtil

internal class HomeNavigationItemDiffCallback(
    private val navigationItems: List<HomeNavigationItem>,
    private val menu: Menu
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val menuItem = menu.getItem(oldItemPosition)
        val navItem = navigationItems[newItemPosition]
        return menuItem.itemId == navItem.destinationId
    }

    override fun getOldListSize(): Int = menu.size()

    override fun getNewListSize(): Int = navigationItems.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldItemPosition, newItemPosition)
    }
}