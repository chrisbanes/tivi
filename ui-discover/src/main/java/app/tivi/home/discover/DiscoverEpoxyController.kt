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
import app.tivi.common.epoxy.tiviCarousel
import app.tivi.common.epoxy.withModelsFrom
import app.tivi.common.layouts.HeaderBindingModel_
import app.tivi.common.layouts.PosterCardItemBindingModel_
import app.tivi.common.layouts.emptyState
import app.tivi.common.layouts.header
import app.tivi.data.Entry
import app.tivi.data.entities.findHighestRatedPoster
import app.tivi.data.resultentities.EntryWithShow
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject

class DiscoverEpoxyController @Inject constructor(
    private val context: Context,
    private val textCreator: DiscoverTextCreator
) : TypedEpoxyController<DiscoverViewState>() {
    var callbacks: Callbacks? = null

    interface Callbacks {
        fun onTrendingHeaderClicked()
        fun onPopularHeaderClicked()
        fun onRecommendedHeaderClicked()
        fun onNextEpisodeToWatchClicked()
        fun onItemClicked(viewHolderId: Long, item: EntryWithShow<out Entry>)
    }

    override fun buildModels(viewState: DiscoverViewState) {
        val trendingShows = viewState.trendingItems
        val popularShows = viewState.popularItems
        val recommendedShows = viewState.recommendedItems
        val tmdbImageUrlProvider = viewState.tmdbImageUrlProvider

        if (viewState.nextEpisodeWithShowToWatched != null) {
            header {
                id("keep_watching_header")
                title(R.string.discover_keep_watching)
                spanSizeOverride(TotalSpanOverride)
            }
            discoverNextShowEpisodeToWatch {
                id("keep_watching_${viewState.nextEpisodeWithShowToWatched.episode.id}")
                spanSizeOverride(TotalSpanOverride)
                episode(viewState.nextEpisodeWithShowToWatched.episode)
                season(viewState.nextEpisodeWithShowToWatched.season)
                tmdbImageUrlProvider(viewState.tmdbImageUrlProvider)
                tiviShow(viewState.nextEpisodeWithShowToWatched.show)
                posterImage(viewState.nextEpisodeWithShowToWatched.images.findHighestRatedPoster())
                textCreator(textCreator)
                clickListener { _ -> callbacks?.onNextEpisodeToWatchClicked() }
            }
        }

        header {
            id("trending_header")
            title(R.string.discover_trending)
            showProgress(viewState.trendingRefreshing)
            spanSizeOverride(TotalSpanOverride)
            buttonClickListener { _ -> callbacks?.onTrendingHeaderClicked() }
        }
        if (trendingShows.isNotEmpty()) {
            tiviCarousel {
                id("trending_carousel")
                itemWidth(context.resources.getDimensionPixelSize(R.dimen.discover_carousel_item_width))
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

        if (recommendedShows.isNotEmpty()) {
            header {
                id("recommended_header")
                title(R.string.discover_recommended)
                showProgress(viewState.recommendedRefreshing)
                spanSizeOverride(TotalSpanOverride)
                buttonClickListener { _ -> callbacks?.onRecommendedHeaderClicked() }
            }
            tiviCarousel {
                id("recommended_carousel")
                itemWidth(context.resources.getDimensionPixelSize(R.dimen.discover_carousel_item_width))
                hasFixedSize(true)

                val vert = context.resources.getDimensionPixelSize(R.dimen.spacing_small)
                val horiz = context.resources.getDimensionPixelSize(R.dimen.spacing_normal)
                val itemSpacing = context.resources.getDimensionPixelSize(R.dimen.spacing_micro)
                padding(Carousel.Padding(horiz, vert, horiz, vert, itemSpacing))

                withModelsFrom(recommendedShows) { item ->
                    PosterCardItemBindingModel_().apply {
                        id(item.generateStableId())
                        tmdbImageUrlProvider(tmdbImageUrlProvider)
                        tiviShow(item.show)
                        posterImage(item.images.findHighestRatedPoster())
                        transitionName("recommended_${item.show.homepage}")
                        clickListener { model, _, _, _ ->
                            callbacks?.onItemClicked(model.id(), item)
                        }
                    }
                }
            }
        }

        header {
            id("popular_header")
            title(R.string.discover_popular)
            showProgress(viewState.popularRefreshing)
            spanSizeOverride(TotalSpanOverride)
            buttonClickListener { _ -> callbacks?.onPopularHeaderClicked() }
        }
        if (popularShows.isNotEmpty()) {
            tiviCarousel {
                id("popular_carousel")
                itemWidth(context.resources.getDimensionPixelSize(R.dimen.discover_carousel_item_width))
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