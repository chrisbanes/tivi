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

package me.banes.chris.tivi.showdetails.details

import android.content.Context
import android.view.View
import com.airbnb.epoxy.Typed4EpoxyController
import me.banes.chris.tivi.PosterGridItemBindingModel_
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.entities.SeasonWithEpisodes
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.detailsBadge
import me.banes.chris.tivi.detailsSummary
import me.banes.chris.tivi.detailsTitle
import me.banes.chris.tivi.emptyState
import me.banes.chris.tivi.header
import me.banes.chris.tivi.seasonHeader
import me.banes.chris.tivi.tmdb.TmdbImageUrlProvider
import me.banes.chris.tivi.ui.epoxy.TotalSpanOverride
import me.banes.chris.tivi.ui.epoxy.carousel
import me.banes.chris.tivi.ui.epoxy.withModelsFrom

class ShowDetailsEpoxyController(
    private val context: Context,
    private val callbacks: Callbacks
) : Typed4EpoxyController<TiviShow, List<TiviShow>, List<SeasonWithEpisodes>, TmdbImageUrlProvider>() {

    interface Callbacks {
        fun onRelatedShowClicked(show: TiviShow, view: View)
    }

    override fun buildModels(
        show: TiviShow,
        related: List<TiviShow>,
        seasons: List<SeasonWithEpisodes>,
        tmdbImageUrlProvider: TmdbImageUrlProvider
    ) {
        detailsTitle {
            id("title")
            title(show.title)
            subtitle(show.originalTitle)
            genres(show.genres)
            spanSizeOverride(TotalSpanOverride)
        }

        show.rating?.let { rating ->
            detailsBadge {
                val ratingOutOfOneHundred = Math.round(rating * 10)
                id("rating")
                label(context.getString(R.string.percentage_format, ratingOutOfOneHundred))
                icon(R.drawable.ic_details_rating)
                contentDescription(context.getString(R.string.rating_content_description_format, ratingOutOfOneHundred))
            }
        }
        show.network?.let { network ->
            detailsBadge {
                id("network")
                label(network)
                icon(R.drawable.ic_details_network)
                contentDescription(context.getString(R.string.network_content_description_format, network))
            }
        }
        show.certification?.let { certificate ->
            detailsBadge {
                id("cert")
                label(certificate)
                icon(R.drawable.ic_details_certificate)
                contentDescription(context.getString(R.string.certificate_content_description_format, certificate))
            }
        }
        show.runtime?.let { runtime ->
            detailsBadge {
                val runtimeMinutes = context.getString(R.string.minutes_format, runtime)
                id("runtime")
                label(runtimeMinutes)
                icon(R.drawable.ic_details_runtime)
                contentDescription(context.resources?.getQuantityString(R.plurals.runtime_content_description_format, runtime, runtime))
            }
        }

        detailsSummary {
            id("summary")
            summary(show.summary)
            spanSizeOverride(TotalSpanOverride)
        }

        header {
            id("related_header")
            title(R.string.details_related)
            spanSizeOverride(TotalSpanOverride)
        }

        if (related.isEmpty()) {
            emptyState {
                id("related_empty")
                spanSizeOverride(TotalSpanOverride)
            }
        } else {
            carousel {
                id("related_shows")
                numViewsToShowOnScreen(4f)
                paddingDp(4)
                hasFixedSize(true)
                withModelsFrom(related) { relatedShow ->
                    PosterGridItemBindingModel_()
                            .id("related_${relatedShow.id}")
                            .title(relatedShow.title)
                            .tmdbImageUrlProvider(tmdbImageUrlProvider)
                            .posterPath(relatedShow.tmdbPosterPath)
                            .clickListener { view ->
                                callbacks.onRelatedShowClicked(relatedShow, view)
                            }
                }
            }
        }

        if (seasons.isNotEmpty()) {
            seasons.forEach { season ->
                seasonHeader {
                    id("season_${season.season!!.id}_header")
                    title(season.season?.title)
                    spanSizeOverride(TotalSpanOverride)
                }
            }
        }
    }
}