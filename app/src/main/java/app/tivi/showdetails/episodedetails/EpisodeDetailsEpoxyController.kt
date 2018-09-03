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

package app.tivi.showdetails.episodedetails

import android.content.Context
import app.tivi.R
import app.tivi.detailsBadge
import app.tivi.detailsHeader
import app.tivi.epDetailsSummary
import app.tivi.epDetailsWatchItem
import app.tivi.ui.epoxy.TotalSpanOverride
import app.tivi.util.TiviDateFormatter
import com.airbnb.epoxy.TypedEpoxyController
import com.airbnb.mvrx.Success

class EpisodeDetailsEpoxyController(
    private val context: Context,
    private val dateFormatter: TiviDateFormatter,
    private val callbacks: Callbacks
) : TypedEpoxyController<EpisodeDetailsViewState>() {

    interface Callbacks {
        // TODO
    }

    override fun buildModels(viewState: EpisodeDetailsViewState) {
        when (viewState.episode) {
            is Success -> {
                val episode = viewState.episode()!!
                episode.traktRating?.also { rating ->
                    detailsBadge {
                        val ratingOutOfOneHundred = Math.round(rating * 10)
                        id("rating")
                        label(context.getString(R.string.percentage_format, ratingOutOfOneHundred))
                        icon(R.drawable.ic_details_rating)
                        contentDescription(context.getString(
                                R.string.rating_content_description_format, ratingOutOfOneHundred))
                    }
                }
                episode.firstAired?.also { firstAired ->
                    detailsBadge {
                        id("aired")
                        label(dateFormatter.formatShortRelativeTime(firstAired))
                        icon(R.drawable.ic_details_date)
                    }
                }
                epDetailsSummary {
                    id("episode_summary")
                    episode(episode)
                    spanSizeOverride(TotalSpanOverride)
                }
            }
        }

        when (viewState.watches) {
            is Success -> {
                val watches = viewState.watches()!!
                detailsHeader {
                    id("watches_header")
                    title(R.string.episode_watches)
                    spanSizeOverride(TotalSpanOverride)
                }
                for (entry in watches) {
                    epDetailsWatchItem {
                        id("watch_${entry.id}")
                        dateTimeFormatter(dateFormatter)
                        watch(entry)
                        spanSizeOverride(TotalSpanOverride)
                    }
                }
            }
        }
    }
}