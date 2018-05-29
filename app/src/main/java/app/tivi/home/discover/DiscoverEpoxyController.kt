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

import android.view.View
import com.airbnb.epoxy.Typed3EpoxyController
import app.tivi.R
import app.tivi.data.Entry
import app.tivi.data.entities.ListItem
import app.tivi.data.entities.PopularEntry
import app.tivi.data.entities.TrendingEntry
import app.tivi.emptyState
import app.tivi.header
import app.tivi.posterGridItem
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.epoxy.TotalSpanOverride

class DiscoverEpoxyController(
    private val callbacks: Callbacks
) : Typed3EpoxyController<List<ListItem<TrendingEntry>>, List<ListItem<PopularEntry>>, TmdbImageUrlProvider>() {

    interface Callbacks {
        fun onTrendingHeaderClicked(items: List<ListItem<TrendingEntry>>?)
        fun onPopularHeaderClicked(items: List<ListItem<PopularEntry>>?)
        fun onItemClicked(item: ListItem<out Entry>)
    }

    override fun buildModels(
        trending: List<ListItem<TrendingEntry>>?,
        popular: List<ListItem<PopularEntry>>?,
        tmdbImageUrlProvider: TmdbImageUrlProvider?
    ) {
        header {
            id("trending_header")
            title(R.string.discover_trending)
            spanSizeOverride(TotalSpanOverride)
            buttonClickListener(View.OnClickListener {
                callbacks.onTrendingHeaderClicked(trending)
            })
        }
        if (trending != null && !trending.isEmpty()) {
            trending.take(spanCount * 2).forEach { item ->
                posterGridItem {
                    id(item.generateStableId())
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    posterPath(item.show?.tmdbPosterPath)
                    title(item.show?.title)
                    annotationLabel(item.entry?.watchers.toString())
                    annotationIcon(R.drawable.ic_eye_12dp)
                    transitionName("trending_${item.show?.homepage}")
                    clickListener(View.OnClickListener {
                        callbacks.onItemClicked(item)
                    })
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
            buttonClickListener(View.OnClickListener {
                callbacks.onPopularHeaderClicked(popular)
            })
        }
        if (popular != null && !popular.isEmpty()) {
            popular.take(spanCount * 2).forEach { item ->
                posterGridItem {
                    id(item.generateStableId())
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    posterPath(item.show?.tmdbPosterPath)
                    title(item.show?.title)
                    transitionName("popular_${item.show?.homepage}")
                    clickListener(View.OnClickListener {
                        callbacks.onItemClicked(item)
                    })
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