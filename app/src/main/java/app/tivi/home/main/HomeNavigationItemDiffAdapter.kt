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
import androidx.recyclerview.widget.ListUpdateCallback

internal class HomeNavigationItemDiffAdapter(
    private val navigationItems: List<HomeNavigationItem>,
    private val menu: Menu
) : ListUpdateCallback {
    override fun onChanged(position: Int, count: Int, payload: Any?) {}

    override fun onMoved(fromPosition: Int, toPosition: Int) {}

    override fun onInserted(startPosition: Int, count: Int) {
        (startPosition until startPosition + count).forEach { position ->
            val navItem = navigationItems[position]
            menu.add(
                    Menu.NONE,
                    navItem.destinationId,
                    position,
                    navItem.labelResId
            ).apply {
                setIcon(navItem.iconResId)
            }
        }
    }

    override fun onRemoved(startPosition: Int, count: Int) {
        (startPosition + count - 1 downTo startPosition)
                .map { menu.getItem(it) }
                .forEach { menu.removeItem(it.itemId) }
    }
}