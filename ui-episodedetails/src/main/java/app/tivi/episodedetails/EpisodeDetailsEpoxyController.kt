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
import app.tivi.util.TiviDateFormatter
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.TypedEpoxyController
import javax.inject.Inject
import kotlin.math.roundToInt

internal class EpisodeDetailsEpoxyController @Inject constructor(
    @PerActivity private val context: Context,
    private val dateFormatter: TiviDateFormatter
) : TypedEpoxyController<EpisodeDetailsViewState>() {

    override fun buildModels(viewState: EpisodeDetailsViewState) {
        if (viewState.episode != null) {
            val episode = viewState.episode

            val badges = ArrayList<EpoxyModel<*>>()
            episode.traktRating?.also { rating ->
                badges += EpisodeDetailsBadgeBindingModel_().apply {
                    val ratingOutOfOneHundred = (rating * 10).roundToInt()
                    id("rating")
                    label(context.getString(R.string.percentage_format, ratingOutOfOneHundred))
                    icon(R.drawable.ic_details_rating)
                    contentDescription(context.getString(
                            R.string.rating_content_description_format, ratingOutOfOneHundred))
                }
            }
            episode.firstAired?.also { firstAired ->
                badges += EpisodeDetailsBadgeBindingModel_().apply {
                    id("aired")
                    label(dateFormatter.formatShortRelativeTime(firstAired))
                    icon(R.drawable.ic_details_date)
                }
            }
            if (badges.isNotEmpty()) {
                EpoxyModelGroup(R.layout.layout_badge_holder, badges).addTo(this)
            }

            episodeDetailsSummary {
                id("episode_summary")
                episode(episode)
            }
        }

        val watches = viewState.watches
        if (watches.isNotEmpty()) {
            detailsHeader {
                id("watches_header")
                title(R.string.episode_watches)
            }
            watches.forEach { entry ->
                episodeDetailsWatchItem {
                    id("watch_${entry.id}")
                    dateTimeFormatter(dateFormatter)
                    watch(entry)
                }
            }
        }
    }
}
