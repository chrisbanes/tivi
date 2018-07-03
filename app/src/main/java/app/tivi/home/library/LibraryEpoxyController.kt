/*
 * Copyright 2017 Google, Inc.
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
import app.tivi.data.Entry
import app.tivi.data.entities.EntryWithShow
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.WatchedShowEntry
import app.tivi.emptyState
import app.tivi.posterGridItem
import app.tivi.ui.epoxy.TotalSpanOverride
import com.airbnb.epoxy.TypedEpoxyController

class LibraryEpoxyController(private val callbacks: Callbacks) : TypedEpoxyController<LibraryViewState>() {
    interface Callbacks {
        fun onMyShowsHeaderClicked(items: List<EntryWithShow<FollowedShowEntry>>?)
        fun onWatchedHeaderClicked(items: List<EntryWithShow<WatchedShowEntry>>?)
        fun onItemClicked(item: EntryWithShow<out Entry>)
    }

    override fun buildModels(viewState: LibraryViewState) {
        when {
            viewState is LibraryFollowedViewState -> {
                if (viewState.followedShows.isNotEmpty()) {
                    viewState.followedShows.forEach { item ->
                        posterGridItem {
                            id(item.generateStableId())
                            tmdbImageUrlProvider(viewState.tmdbImageUrlProvider)
                            posterPath(item.show.tmdbPosterPath)
                            title(item.show.title)
                            transitionName("myshows_${item.show.homepage}")
                            clickListener(View.OnClickListener {
                                callbacks.onItemClicked(item)
                            })
                        }
                    }
                } else {
                    emptyState {
                        id("myshows_placeholder")
                        spanSizeOverride(TotalSpanOverride)
                    }
                }
            }
            viewState is LibraryWatchedViewState -> {
                if (viewState.watchedShows.isNotEmpty()) {
                    viewState.watchedShows.forEach { item ->
                        posterGridItem {
                            id(item.generateStableId())
                            tmdbImageUrlProvider(viewState.tmdbImageUrlProvider)
                            posterPath(item.show.tmdbPosterPath)
                            title(item.show.title)
                            transitionName("watched_${item.show.homepage}")
                            clickListener(View.OnClickListener {
                                callbacks.onItemClicked(item)
                            })
                        }
                    }
                } else {
                    emptyState {
                        id("watched_placeholder")
                        spanSizeOverride(TotalSpanOverride)
                    }
                }
            }
        }
    }
}