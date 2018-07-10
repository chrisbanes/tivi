/*
 * Copyright 2018 Google, Inc.
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
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.emptyState
import app.tivi.libraryFollowedItem
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.epoxy.EpoxyModelProperty
import app.tivi.ui.epoxy.TotalSpanOverride
import com.airbnb.epoxy.paging.PagingEpoxyController

class LibraryFollowedEpoxyController(
    private val callbacks: Callbacks
) : PagingEpoxyController<FollowedShowEntryWithShow>() {
    var tmdbImageUrlProvider by EpoxyModelProperty { TmdbImageUrlProvider() }
    var isEmpty by EpoxyModelProperty { false }

    override fun buildModels(list: List<FollowedShowEntryWithShow>) {
        if (isEmpty) {
            emptyState {
                id("placeholder")
                spanSizeOverride(TotalSpanOverride)
            }
        } else {
            list.forEach { item ->
                libraryFollowedItem {
                    id(item.generateStableId())
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    tiviShow(item.show)
                    posterTransitionName("show_${item.show.homepage}")
                    spanSizeOverride(TotalSpanOverride)
                    clickListener(View.OnClickListener {
                        callbacks.onItemClicked(item)
                    })
                }
            }
        }
    }

    interface Callbacks {
        fun onItemClicked(item: FollowedShowEntryWithShow)
    }
}