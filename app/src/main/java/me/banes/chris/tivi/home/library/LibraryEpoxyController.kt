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

package me.banes.chris.tivi.home.library

import android.view.View
import com.airbnb.epoxy.Typed3EpoxyController
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.data.entities.MyShowsEntry
import me.banes.chris.tivi.data.entities.WatchedEntry
import me.banes.chris.tivi.emptyState
import me.banes.chris.tivi.header
import me.banes.chris.tivi.posterGridItem
import me.banes.chris.tivi.tmdb.TmdbImageUrlProvider
import me.banes.chris.tivi.ui.epoxy.TotalSpanOverride

class LibraryEpoxyController(
    private val callbacks: Callbacks
) : Typed3EpoxyController<List<ListItem<MyShowsEntry>>, List<ListItem<WatchedEntry>>, TmdbImageUrlProvider>() {

    interface Callbacks {
        fun onMyShowsHeaderClicked(items: List<ListItem<MyShowsEntry>>?)
        fun onWatchedHeaderClicked(items: List<ListItem<WatchedEntry>>?)
        fun onItemClicked(item: ListItem<out Entry>)
    }

    override fun buildModels(
        myShows: List<ListItem<MyShowsEntry>>?,
        watched: List<ListItem<WatchedEntry>>?,
        tmdbImageUrlProvider: TmdbImageUrlProvider?
    ) {
        header {
            id("myshows_header")
            title(R.string.library_myshows)
            spanSizeOverride(TotalSpanOverride)
            buttonClickListener(View.OnClickListener {
                callbacks.onMyShowsHeaderClicked(myShows)
            })
        }
        if (myShows != null && !myShows.isEmpty()) {
            myShows.take(spanCount * 2).forEach { item ->
                posterGridItem {
                    id(item.generateStableId())
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    posterPath(item.show?.tmdbPosterPath)
                    transitionName("myshows_${item.show?.homepage}")
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

        header {
            id("watched_header")
            title(R.string.library_watched)
            spanSizeOverride(TotalSpanOverride)
            buttonClickListener(View.OnClickListener {
                callbacks.onWatchedHeaderClicked(watched)
            })
        }
        if (watched != null && !watched.isEmpty()) {
            watched.take(spanCount * 2).forEach { item ->
                posterGridItem {
                    id(item.generateStableId())
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    posterPath(item.show?.tmdbPosterPath)
                    transitionName("watched_${item.show?.homepage}")
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