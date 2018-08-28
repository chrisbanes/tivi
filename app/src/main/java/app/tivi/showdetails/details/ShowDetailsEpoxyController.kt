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

package app.tivi.showdetails.details

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.util.ArraySet
import android.view.View
import app.tivi.DetailsRelatedItemBindingModel_
import app.tivi.R
import app.tivi.data.entities.ActionDate
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.databinding.ViewHolderDetailsSeasonBinding
import app.tivi.detailsBadge
import app.tivi.detailsHeader
import app.tivi.detailsSeason
import app.tivi.detailsSummary
import app.tivi.emptyState
import app.tivi.seasonEpisodeItem
import app.tivi.ui.epoxy.TotalSpanOverride
import app.tivi.ui.epoxy.carousel
import app.tivi.ui.epoxy.withModelsFrom
import app.tivi.ui.widget.PopupMenuButton
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyController

class ShowDetailsEpoxyController(
    private val context: Context,
    private val callbacks: Callbacks
) : EpoxyController() {
    var state: ShowDetailsViewState? = null
        set(value) {
            field = value
            requestModelBuild()
        }

    private val expandedSeasons = ArraySet<Long>()

    interface Callbacks {
        fun onRelatedShowClicked(show: TiviShow, view: View)
        fun onEpisodeClicked(episode: Episode, view: View)
        fun onMarkSeasonWatched(season: Season, onlyAired: Boolean, date: ActionDate)
        fun onMarkSeasonUnwatched(season: Season)
    }

    override fun buildModels() {
        state?.also(this::buildModels)
    }

    private fun buildModels(viewState: ShowDetailsViewState) {
        val show = viewState.show
        val tmdbImageUrlProvider = viewState.tmdbImageUrlProvider
        val related = viewState.relatedShows

        show.traktRating?.let { rating ->
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
            entity(show)
            spanSizeOverride(TotalSpanOverride)
        }

        detailsHeader {
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
                numViewsToShowOnScreen(5.25f)
                hasFixedSize(true)

                val small = context.resources.getDimensionPixelSize(R.dimen.spacing_small)
                val micro = context.resources.getDimensionPixelSize(R.dimen.spacing_micro)
                padding(Carousel.Padding(micro, micro, small, small, micro))

                withModelsFrom(related) { relatedEntry ->
                    val relatedShow = relatedEntry.show
                    DetailsRelatedItemBindingModel_()
                            .id("related_${relatedShow.id}")
                            .tiviShow(relatedShow)
                            .tmdbImageUrlProvider(tmdbImageUrlProvider)
                            .clickListener { view ->
                                callbacks.onRelatedShowClicked(relatedShow, view)
                            }
                }
            }
        }

        if (viewState.seasons.isNotEmpty()) {
            detailsHeader {
                id("title_seasons")
                title(R.string.show_details_seasons)
                spanSizeOverride(TotalSpanOverride)
            }

            for (season in viewState.seasons) {
                detailsSeason {
                    id("season_${season.season!!.id}")
                    season(season)
                    spanSizeOverride(TotalSpanOverride)
                    clickListener { _ -> toggleSeasonExpanded(season.season!!.id) }

                    popupMenuListener(SeasonPopupMenuListener(season))

                    popupMenuClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.season_mark_all_watched_now -> {
                                callbacks.onMarkSeasonWatched(season.season!!, false, ActionDate.NOW)
                            }
                            R.id.season_mark_all_watched_air_date -> {
                                callbacks.onMarkSeasonWatched(season.season!!, false, ActionDate.AIR_DATE)
                            }
                            R.id.season_mark_aired_watched_now -> {
                                callbacks.onMarkSeasonWatched(season.season!!, true, ActionDate.NOW)
                            }
                            R.id.season_mark_aired_watched_air_date -> {
                                callbacks.onMarkSeasonWatched(season.season!!, true, ActionDate.AIR_DATE)
                            }
                            R.id.season_mark_all_unwatched -> {
                                callbacks.onMarkSeasonUnwatched(season.season!!)
                            }
                        }
                        true
                    }

                    onBind { _, view, _ ->
                        val binding = view.dataBinding as ViewHolderDetailsSeasonBinding
                        val listener = binding.popupMenuListener as SeasonPopupMenuListener
                        listener.season = binding.season!!
                    }
                }

                if (isSeasonExpanded(season.season!!.id)) {
                    season.episodes.forEach { episodeWithWatches ->
                        seasonEpisodeItem {
                            val episode = episodeWithWatches.episode!!
                            id("episode_${episode.id}")
                            episodeWithWatches(episodeWithWatches)
                            spanSizeOverride(TotalSpanOverride)
                            clickListener { view ->
                                callbacks.onEpisodeClicked(episode, view)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isSeasonExpanded(seasonId: Long) = expandedSeasons.contains(seasonId)

    private fun toggleSeasonExpanded(seasonId: Long) {
        if (expandedSeasons.contains(seasonId)) {
            expandedSeasons.remove(seasonId)
        } else {
            expandedSeasons.add(seasonId)
        }
        // Trigger a model refresh
        requestModelBuild()
    }

    private class SeasonPopupMenuListener(
        var season: SeasonWithEpisodesAndWatches
    ) : PopupMenuButton.PopupMenuListener {
        override fun onPreparePopupMenu(popupMenu: PopupMenu) {
            popupMenu.menu.findItem(R.id.season_mark_all_unwatched).also {
                it.isVisible = season.numberWatched > 0
            }
            popupMenu.menu.findItem(R.id.season_mark_watched_all).also {
                it.isVisible = season.numberWatched < season.numberEpisodes
            }
            popupMenu.menu.findItem(R.id.season_mark_watched_aired).also {
                it.isVisible = season.numberWatched < season.numberAired && season.numberAired < season.numberEpisodes
            }
        }
    }
}