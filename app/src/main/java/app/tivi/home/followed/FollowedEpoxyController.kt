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

package app.tivi.home.followed

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach
import app.tivi.HeaderBindingModel_
import app.tivi.LibraryFollowedItemBindingModel_
import app.tivi.R
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.emptyState
import app.tivi.filter
import app.tivi.header
import app.tivi.home.HomeTextCreator
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.epoxy.EpoxyModelProperty
import app.tivi.ui.epoxy.TotalSpanOverride
import app.tivi.ui.widget.PopupMenuButton
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController
import javax.inject.Inject

class FollowedEpoxyController @Inject constructor(
    private val textCreator: HomeTextCreator
) : PagedListEpoxyController<FollowedShowEntryWithShow>(
        modelBuildingHandler = Handler(Looper.getMainLooper()),
        diffingHandler = EpoxyAsyncUtil.getAsyncBackgroundHandler()
) {
    var tmdbImageUrlProvider by EpoxyModelProperty { TmdbImageUrlProvider() }
    var isEmpty by EpoxyModelProperty { false }
    var filter by EpoxyModelProperty<CharSequence> { "" }
    var callbacks: Callbacks? = null
    var sortOptions by EpoxyModelProperty { emptyList<SortOption>() }

    override fun addModels(models: List<EpoxyModel<*>>) {
        if (isEmpty) {
            emptyState {
                id("empty")
                spanSizeOverride(TotalSpanOverride)
            }
        } else {
            header {
                id("header")
                titleString(textCreator.showHeaderCount(models.size))
            }
            filter {
                id("filters")
                filter(filter)
                watcher(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        callbacks?.onFilterChanged(s?.toString() ?: "")
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                popupMenuListener(object : PopupMenuButton.PopupMenuListener {
                    override fun onPreparePopupMenu(popupMenu: PopupMenu) {
                        popupMenu.menu.forEach {
                            when (it.itemId) {
                                R.id.popup_sort_last_watched -> {
                                    it.isVisible = sortOptions.contains(SortOption.LAST_WATCHED)
                                }
                                R.id.popup_sort_date_followed -> {
                                    it.isVisible = sortOptions.contains(SortOption.DATE_ADDED)
                                }
                                R.id.popup_sort_alpha -> {
                                    it.isVisible = sortOptions.contains(SortOption.ALPHABETICAL)
                                }
                            }
                        }
                    }
                })

                popupMenuClickListener {
                    val option = when (it.itemId) {
                        R.id.popup_sort_date_followed -> SortOption.DATE_ADDED
                        R.id.popup_sort_alpha -> SortOption.ALPHABETICAL
                        else -> SortOption.LAST_WATCHED
                    }
                    callbacks?.onSortSelected(option)
                    true
                }
            }
            super.addModels(models)
        }
    }

    override fun buildItemModel(currentPosition: Int, item: FollowedShowEntryWithShow?): EpoxyModel<*> {
        return LibraryFollowedItemBindingModel_().apply {
            if (item != null) {
                id(item.generateStableId())
                tiviShow(item.show)
                posterTransitionName("show_${item.show.homepage}")
                clickListener(View.OnClickListener {
                    callbacks?.onItemClicked(item)
                })
            } else {
                id("item_placeholder_$currentPosition")
            }
            followedEntry(item?.entry)
            textCreator(textCreator)
            tmdbImageUrlProvider(tmdbImageUrlProvider)
        }
    }

    fun isHeader(model: EpoxyModel<*>): Boolean {
        return model is HeaderBindingModel_
    }

    interface Callbacks {
        fun onItemClicked(item: FollowedShowEntryWithShow)
        fun onFilterChanged(filter: String)
        fun onSortSelected(sort: SortOption)
    }
}