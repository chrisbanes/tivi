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

import android.view.View
import app.tivi.homeNavItem
import app.tivi.ui.epoxy.EpoxyModelProperty
import com.airbnb.epoxy.EpoxyController

internal class HomeNavigationEpoxyController(
    private val callbacks: Callbacks
) : EpoxyController() {
    var items: List<HomeNavigationItem> by EpoxyModelProperty { emptyList<HomeNavigationItem>() }
    var selectedItem: HomeNavigationItem? by EpoxyModelProperty { null }

    interface Callbacks {
        fun onNavigationItemSelected(item: HomeNavigationItem)
    }

    override fun buildModels() {
        items.forEach { item ->
            homeNavItem {
                id("item_${item.name}")
                navItem(item)
                isSelected(item == selectedItem)
                clickListener(View.OnClickListener {
                    callbacks.onNavigationItemSelected(item)
                })
            }
        }
    }
}