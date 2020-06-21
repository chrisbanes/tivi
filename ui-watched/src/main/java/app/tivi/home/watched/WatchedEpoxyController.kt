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

package app.tivi.home.watched

import android.text.Editable
import android.text.TextWatcher
import app.tivi.common.epoxy.TotalSpanOverride
import app.tivi.common.layouts.emptyState
import app.tivi.common.layouts.filter
import app.tivi.common.layouts.vertSpacerNormal
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.extensions.observable
import app.tivi.home.HomeTextCreator
import app.tivi.ui.SortPopupMenuListener
import app.tivi.ui.popupMenuItemIdToSortOption
import app.tivi.util.TiviDateFormatter
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import javax.inject.Inject

internal class WatchedEpoxyController @Inject constructor(
    private val textCreator: HomeTextCreator,
    private val dateFormatter: TiviDateFormatter
) : PagedListEpoxyController<WatchedShowEntryWithShow>() {
    var state by observable(WatchedViewState(), ::requestModelBuild)
    var callbacks: Callbacks? by observable(null, ::requestModelBuild)

    override fun addModels(models: List<EpoxyModel<*>>) {
        if (state.isEmpty) {
            emptyState {
                id("empty")
                spanSizeOverride(TotalSpanOverride)
            }
        } else {
            vertSpacerNormal {
                id("top_spacer")
            }

            filter {
                id("filters")
                filter(state.filter)
                numberShows(models.size)
                watcher(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        callbacks?.onFilterChanged(s?.toString() ?: "")
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }
                })

                popupMenuListener(SortPopupMenuListener(state.sort, state.availableSorts))
                popupMenuClickListener {
                    val option = popupMenuItemIdToSortOption(it.itemId)
                        ?: throw IllegalArgumentException("Selected sort option is null")
                    callbacks?.onSortSelected(option)
                    true
                }
            }

            super.addModels(models)

            vertSpacerNormal {
                id("bottom_spacer")
            }
        }
    }

    override fun buildItemModel(
        currentPosition: Int,
        item: WatchedShowEntryWithShow?
    ): EpoxyModel<*> {
        return LibraryWatchedItemBindingModel_().apply {
            if (item != null) {
                id(item.generateStableId())
                tiviShow(item.show)
                posterImage(item.poster)
                posterTransitionName("show_${item.show.homepage}")
                selected(item.show.id in state.selectedShowIds)
                clickListener { _ -> callbacks?.onItemClicked(item) }
                longClickListener { _ -> callbacks?.onItemLongClicked(item) ?: false }
            } else {
                id("item_placeholder_$currentPosition")
            }
            watchedEntry(item?.entry)
            dateFormatter(dateFormatter)
            textCreator(textCreator)
        }
    }

    fun clear() {
        callbacks = null
    }

    interface Callbacks {
        fun onItemClicked(item: WatchedShowEntryWithShow)
        fun onItemLongClicked(item: WatchedShowEntryWithShow): Boolean
        fun onFilterChanged(filter: String)
        fun onSortSelected(sort: SortOption)
    }
}
