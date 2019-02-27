/*
 * Copyright 2018 Google LLC
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

package app.tivi.home.library

import android.view.View
import app.tivi.libraryFilterItem
import com.airbnb.epoxy.TypedEpoxyController

class LibraryFiltersEpoxyController(private val callbacks: Callbacks) : TypedEpoxyController<LibraryViewState>() {
    interface Callbacks {
        fun onFilterSelected(filter: LibraryFilter)
    }

    override fun buildModels(viewState: LibraryViewState) {
        viewState.allowedFilters.forEach { filter ->
            libraryFilterItem {
                id("filter_${filter.name}")
                filter(filter)
                isSelected(filter == viewState.filter)
                clickListener(View.OnClickListener {
                    callbacks.onFilterSelected(filter)
                })
            }
        }
    }
}