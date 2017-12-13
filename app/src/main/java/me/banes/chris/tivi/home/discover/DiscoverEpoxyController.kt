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

package me.banes.chris.tivi.home.discover

import android.view.View
import com.airbnb.epoxy.Typed3EpoxyController
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.data.entities.PopularEntry
import me.banes.chris.tivi.data.entities.TrendingEntry
import me.banes.chris.tivi.tmdb.TmdbImageUrlProvider
import me.banes.chris.tivi.ui.epoxymodels.header
import me.banes.chris.tivi.ui.epoxymodels.showPoster

class DiscoverEpoxyController(private val callbacks: DiscoverAdapterCallbacks) : Typed3EpoxyController<
        List<ListItem<TrendingEntry>>,
        List<ListItem<PopularEntry>>,
        TmdbImageUrlProvider>() {

    override fun buildModels(
            trending: List<ListItem<TrendingEntry>>?,
            popular: List<ListItem<PopularEntry>>?,
            tmdbImageUrlProvider: TmdbImageUrlProvider?) {
        header {
            id("trending_header")
            title(R.string.discover_trending)
            clickListener(View.OnClickListener {
                callbacks.onTrendingHeaderClicked(trending)
            })
        }
        if (trending != null && !trending.isEmpty()) {
            trending.take(spanCount * 2).forEach { item ->
                showPoster {
                    id(item.entry?.id)
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    posterPath(item.show?.tmdbPosterPath)
                    annotation(item.entry?.watchers.toString())
                    annotationDrawable(null)
                    transName("trending_${item.show?.homepage}")
                }
            }
        } else {
            // TODO show placeholder
        }

        header {
            id("popular_header")
            title(R.string.discover_popular)
            clickListener(View.OnClickListener {
                callbacks.onPopularHeaderClicked(popular)
            })
        }
        if (popular != null && !popular.isEmpty()) {
            popular.take(spanCount * 2).forEach { item ->
                showPoster {
                    id(item.entry?.id)
                    tmdbImageUrlProvider(tmdbImageUrlProvider)
                    posterPath(item.show?.tmdbPosterPath)
                    annotation(null)
                    annotationDrawable(null)
                    transName("popular_${item.show?.homepage}")
                }
            }
        } else {
            // TODO show placeholder
        }
    }
}

interface DiscoverAdapterCallbacks {
    fun onTrendingHeaderClicked(items: List<ListItem<TrendingEntry>>?)
    fun onPopularHeaderClicked(items: List<ListItem<PopularEntry>>?)
}