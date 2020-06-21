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
import app.tivi.common.layouts.PosterCardItemBindingModel_
import app.tivi.common.layouts.emptyState
import app.tivi.common.layouts.header
import app.tivi.common.layouts.vertSpacerNormal
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.observable
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyController
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

internal class DiscoverEpoxyController @Inject constructor(
    @ActivityContext private val context: Context,
    private val textCreator: DiscoverTextCreator
) : EpoxyController() {
    var callbacks: Callbacks? by observable(null, ::requestModelBuild)
    var state: DiscoverViewState by observable(DiscoverViewState(), ::requestModelBuild)

    interface Callbacks {
        fun onTrendingHeaderClicked()
        fun onPopularHeaderClicked()
        fun onRecommendedHeaderClicked()
        fun onNextEpisodeToWatchClicked()
        fun onItemClicked(viewHolderId: Long, item: EntryWithShow<out Entry>)
    }

    override fun buildModels() {
        val trendingShows = state.trendingItems
        val popularShows = state.popularItems
        val recommendedShows = state.recommendedItems

        vertSpacerNormal {
            id("top_spacer")
        }

        state.nextEpisodeWithShowToWatched?.also { nextEpisodeToWatch ->
            header {
                id("keep_watching_header")
                title(R.string.discover_keep_watching_title)
                spanSizeOverride(TotalSpanOverride)
            }
            discoverNextShowEpisodeToWatch {
                id("keep_watching_${nextEpisodeToWatch.episode.id}")
                spanSizeOverride(TotalSpanOverride)
                episode(nextEpisodeToWatch.episode)
                season(nextEpisodeToWatch.season)
                tiviShow(nextEpisodeToWatch.show)
                posterImage(nextEpisodeToWatch.poster)
                textCreator(textCreator)
                clickListener { _ -> callbacks?.onNextEpisodeToWatchClicked() }
            }
        }

        if (modelCountBuiltSoFar > 1) {
            vertSpacerNormal {
                id("trending_header_spacer")
            }
        }
        header {
            id("trending_header")
            title(R.string.discover_trending_title)
            showProgress(state.trendingRefreshing)
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
                        tiviShow(item.show)
                        posterImage(item.poster)
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
            vertSpacerNormal {
                id("recommended_header_spacer")
            }
            header {
                id("recommended_header")
                title(R.string.discover_recommended_title)
                showProgress(state.recommendedRefreshing)
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
                        tiviShow(item.show)
                        posterImage(item.poster)
                        transitionName("recommended_${item.show.homepage}")
                        clickListener { model, _, _, _ ->
                            callbacks?.onItemClicked(model.id(), item)
                        }
                    }
                }
            }
        }

        vertSpacerNormal {
            id("popular_header_spacer")
        }
        header {
            id("popular_header")
            title(R.string.discover_popular_title)
            showProgress(state.popularRefreshing)
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
                        posterImage(item.poster)
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

        vertSpacerNormal {
            id("bottom_spacer")
        }
    }

    fun clear() {
        callbacks = null
    }
}
