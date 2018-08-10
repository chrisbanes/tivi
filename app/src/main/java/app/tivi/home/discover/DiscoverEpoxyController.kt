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

package app.tivi.home.discover

import app.tivi.R
import app.tivi.data.Entry
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.emptyState
import app.tivi.header
import app.tivi.posterGridItem
import app.tivi.ui.epoxy.TotalSpanOverride
import com.airbnb.epoxy.TypedEpoxyController

class DiscoverEpoxyController(
    private val callbacks: Callbacks
) : TypedEpoxyController<DiscoverViewState>() {

    interface Callbacks {
        fun onTrendingHeaderClicked(items: List<TrendingEntryWithShow>)
        fun onPopularHeaderClicked(items: List<PopularEntryWithShow>)
        fun onItemClicked(viewHolderId: Long, item: EntryWithShow<out Entry>)
        fun onSearchItemClicked(viewHolderId: Long, item: TiviShow)
    }

    override fun buildModels(viewState: DiscoverViewState) {
        when (viewState) {
            is EmptyDiscoverViewState -> buildEmptyModels(viewState)
            is SearchResultDiscoverViewState -> buildSearchResultModels(viewState)
        }
    }

    private fun buildSearchResultModels(viewState: SearchResultDiscoverViewState) {
        val tmdbImageUrlProvider = viewState.tmdbImageUrlProvider

        viewState.results.forEach { result ->
            posterGridItem {
                val id = result.id!!
                id(id)
                tmdbImageUrlProvider(tmdbImageUrlProvider)
                tiviShow(result)
                clickListener { _ ->
                    callbacks.onSearchItemClicked(id, result)
                }
            }
        }
    }

    private fun buildEmptyModels(viewState: EmptyDiscoverViewState) {
        val trendingShows = viewState.trendingItems
        val popularShows = viewState.popularItems
        val tmdbImageUrlProvider = viewState.tmdbImageUrlProvider

        header {
            id("trending_header")
            title(R.string.discover_trending)
            spanSizeOverride(TotalSpanOverride)
            buttonClickListener { _ ->
                callbacks.onTrendingHeaderClicked(trendingShows)
            }
        }
        if (trendingShows.isNotEmpty()) {
            trendingShows.take(spanCount * 2).forEach { item ->
                posterGridItem {
                    val id = item.generateStableId()
                    id(id)
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    tiviShow(item.show)
                    annotationLabel(item.entry?.watchers.toString())
                    annotationIcon(R.drawable.ic_eye_12dp)
                    transitionName("trending_${item.show.homepage}")
                    clickListener { _ ->
                        callbacks.onItemClicked(id, item)
                    }
                }
            }
        } else {
            emptyState {
                id("trending_placeholder")
                spanSizeOverride(TotalSpanOverride)
            }
        }

        header {
            id("popular_header")
            title(R.string.discover_popular)
            spanSizeOverride(TotalSpanOverride)
            buttonClickListener { _ ->
                callbacks.onPopularHeaderClicked(popularShows)
            }
        }
        if (popularShows.isNotEmpty()) {
            popularShows.take(spanCount * 2).forEach { item ->
                posterGridItem {
                    val id = item.generateStableId()
                    id(id)
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    tiviShow(item.show)
                    transitionName("popular_${item.show.homepage}")
                    clickListener { _ ->
                        callbacks.onItemClicked(id, item)
                    }
                }
            }
        } else {
            emptyState {
                id("popular_placeholder")
                spanSizeOverride(TotalSpanOverride)
            }
        }
    }
}