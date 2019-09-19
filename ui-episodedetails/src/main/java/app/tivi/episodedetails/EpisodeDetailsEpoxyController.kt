/*
 * Copyright 2018 Google LLC
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

package app.tivi.episodedetails

import android.content.Context
import app.tivi.common.layouts.detailsHeader
import app.tivi.inject.PerActivity
import app.tivi.common.layouts.DetailsBadgeBindingModel_
import app.tivi.util.TiviDateFormatter
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.TypedEpoxyController
import com.airbnb.mvrx.Success
import javax.inject.Inject
import kotlin.math.roundToInt

class EpisodeDetailsEpoxyController @Inject constructor(
    @PerActivity private val context: Context,
    private val dateFormatter: TiviDateFormatter
) : TypedEpoxyController<EpisodeDetailsViewState>() {

    override fun buildModels(viewState: EpisodeDetailsViewState) {
        when (viewState.episode) {
            is Success -> {
                val episode = viewState.episode()!!

                val badges = ArrayList<DetailsBadgeBindingModel_>()
                episode.traktRating?.also { rating ->
                    badges += DetailsBadgeBindingModel_().apply {
                        val ratingOutOfOneHundred = (rating * 10).roundToInt()
                        id("rating")
                        label(context.getString(R.string.percentage_format, ratingOutOfOneHundred))
                        icon(R.drawable.ic_details_rating)
                        contentDescription(context.getString(
                                R.string.rating_content_description_format, ratingOutOfOneHundred))
                    }
                }
                episode.firstAired?.also { firstAired ->
                    badges += DetailsBadgeBindingModel_().apply {
                        id("aired")
                        label(dateFormatter.formatShortRelativeTime(firstAired))
                        icon(R.drawable.ic_details_date)
                    }
                }
                if (badges.isNotEmpty()) {
                    EpoxyModelGroup(R.layout.layout_badge_holder, badges).addTo(this)
                }

                epDetailsSummary {
                    id("episode_summary")
                    episode(episode)
                }
            }
        }

        val asyncWatches = viewState.watches
        when {
            asyncWatches is Success && asyncWatches().isNotEmpty() -> {
                detailsHeader {
                    id("watches_header")
                    title(R.string.episode_watches)
                }
                for (entry in asyncWatches()) {
                    epDetailsWatchItem {
                        id("watch_${entry.id}")
                        dateTimeFormatter(dateFormatter)
                        watch(entry)
                    }
                }
            }
        }
    }
}