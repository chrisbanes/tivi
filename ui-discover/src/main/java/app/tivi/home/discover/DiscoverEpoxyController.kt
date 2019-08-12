/*
 * Copyright 2017 Google LLC
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

import android.content.Context
import app.tivi.common.epoxy.TotalSpanOverride
import app.tivi.common.epoxy.carousel
import app.tivi.common.epoxy.withModelsFrom
import app.tivi.common.layouts.HeaderBindingModel_
import app.tivi.common.layouts.PosterCardItemBindingModel_
import app.tivi.common.layouts.emptyState
import app.tivi.common.layouts.header
import app.tivi.data.Entry
import app.tivi.data.entities.findHighestRatedPoster
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.data.resultentities.TrendingEntryWithShow
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class DiscoverEpoxyController @Inject constructor(
    private val context: Context
) : TypedEpoxyController<DiscoverViewState>() {
    var callbacks: Callbacks? = null

    interface Callbacks {
        fun onTrendingHeaderClicked(items: List<TrendingEntryWithShow>)
        fun onPopularHeaderClicked(items: List<PopularEntryWithShow>)
        fun onItemClicked(viewHolderId: Long, item: EntryWithShow<out Entry>)
    }

    override fun buildModels(viewState: DiscoverViewState) {
        val trendingShows = viewState.trendingItems
        val popularShows = viewState.popularItems
        val tmdbImageUrlProvider = viewState.tmdbImageUrlProvider

        header {
            id("trending_header")
            title(R.string.discover_trending)
            spanSizeOverride(TotalSpanOverride)
            buttonClickListener { _ ->
                callbacks?.onTrendingHeaderClicked(trendingShows)
            }
        }
        if (trendingShows.isNotEmpty()) {
            carousel {
                id("trending_carousel")
                numViewsToShowOnScreen(3.25f)
                hasFixedSize(true)

                val vert = context.resources.getDimensionPixelSize(R.dimen.spacing_small)
                val horiz = context.resources.getDimensionPixelSize(R.dimen.spacing_normal)
                val itemSpacing = context.resources.getDimensionPixelSize(R.dimen.spacing_micro)
                padding(Carousel.Padding(horiz, vert, horiz, vert, itemSpacing))

                withModelsFrom(trendingShows) { item ->
                    PosterCardItemBindingModel_().apply {
                        id(item.generateStableId())
                        tmdbImageUrlProvider(tmdbImageUrlProvider)
                        tiviShow(item.show)
                        posterImage(item.images.findHighestRatedPoster())
                        transitionName("trending_${item.show.homepage}")
                        clickListener { model, _, _, _ ->
                            callbacks?.onItemClicked(model.id(), item)
                        }
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
                callbacks?.onPopularHeaderClicked(popularShows)
            }
        }
        if (popularShows.isNotEmpty()) {
            carousel {
                id("popular_carousel")
                numViewsToShowOnScreen(3.25f)
                hasFixedSize(true)

                val vert = context.resources.getDimensionPixelSize(R.dimen.spacing_small)
                val horiz = context.resources.getDimensionPixelSize(R.dimen.spacing_normal)
                val itemSpacing = context.resources.getDimensionPixelSize(R.dimen.spacing_micro)
                padding(Carousel.Padding(horiz, vert, horiz, vert, itemSpacing))

                withModelsFrom(popularShows) { item ->
                    PosterCardItemBindingModel_().apply {
                        id(item.generateStableId())
                        tmdbImageUrlProvider(tmdbImageUrlProvider)
                        posterImage(item.images.findHighestRatedPoster())
                        tiviShow(item.show)
                        transitionName("popular_${item.show.homepage}")
                        clickListener { model, _, _, _ ->
                            callbacks?.onItemClicked(model.id(), item)
                        }
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

    fun isHeader(model: EpoxyModel<*>): Boolean {
        return model is HeaderBindingModel_
    }
}