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

package app.tivi.home.library.watched

import android.os.Handler
import android.os.Looper
import android.view.View
import app.tivi.LibraryWatchedItemBindingModel_
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.emptyState
import app.tivi.home.library.LibraryTextCreator
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.epoxy.EpoxyModelProperty
import app.tivi.ui.epoxy.TotalSpanOverride
import app.tivi.util.TiviDateFormatter
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController

class WatchedEpoxyController(
    private val callbacks: Callbacks,
    private val textCreator: LibraryTextCreator,
    private val dateFormatter: TiviDateFormatter
) : PagedListEpoxyController<WatchedShowEntryWithShow>(
        modelBuildingHandler = Handler(Looper.getMainLooper())
) {
    var tmdbImageUrlProvider by EpoxyModelProperty { TmdbImageUrlProvider() }
    var isEmpty by EpoxyModelProperty { false }

    override fun addModels(models: List<EpoxyModel<*>>) {
        if (isEmpty) {
            emptyState {
                id("empty")
                spanSizeOverride(TotalSpanOverride)
            }
        } else {
            super.addModels(models)
        }
    }

    override fun buildItemModel(currentPosition: Int, item: WatchedShowEntryWithShow?): EpoxyModel<*> {
        return LibraryWatchedItemBindingModel_().apply {
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
            watchedEntry(item?.entry)
            dateFormatter(dateFormatter)
            textCreator(textCreator)
            tmdbImageUrlProvider(tmdbImageUrlProvider)
        }
    }

    interface Callbacks {
        fun onItemClicked(item: WatchedShowEntryWithShow)
    }
}