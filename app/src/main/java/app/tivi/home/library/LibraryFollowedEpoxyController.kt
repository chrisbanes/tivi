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
import app.tivi.LibraryFollowedItemBindingModel_
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.emptyState
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.epoxy.EpoxyModelProperty
import app.tivi.ui.epoxy.TotalSpanOverride
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController

class LibraryFollowedEpoxyController(
    private val callbacks: Callbacks,
    private val textCreator: LibraryTextCreator
) : PagedListEpoxyController<FollowedShowEntryWithShow>() {
    var tmdbImageUrlProvider by EpoxyModelProperty { TmdbImageUrlProvider() }
    var isEmpty by EpoxyModelProperty { false }

    override fun addModels(models: List<EpoxyModel<*>>) {
        if (isEmpty) {
            emptyState {
                id("empty")
                spanSizeOverride(TotalSpanOverride)
            }
        } else {
            // Need to use filterNotNull() due to https://github.com/airbnb/epoxy/issues/567
            super.addModels(models.filterNotNull())
        }
    }

    override fun buildItemModel(currentPosition: Int, item: FollowedShowEntryWithShow?): EpoxyModel<*> {
        return LibraryFollowedItemBindingModel_().apply {
            if (item != null) {
                id(item.generateStableId())
                tiviShow(item.show)
                posterTransitionName("show_${item.show.homepage}")
                clickListener(View.OnClickListener {
                    callbacks.onItemClicked(item)
                })
            } else {
                id("item_placeholder_$currentPosition")
            }
            textCreator(textCreator)
            tmdbImageUrlProvider(tmdbImageUrlProvider)
        }
    }

    interface Callbacks {
        fun onItemClicked(item: FollowedShowEntryWithShow)
    }
}