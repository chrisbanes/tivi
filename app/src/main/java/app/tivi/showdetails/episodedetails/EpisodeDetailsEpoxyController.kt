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

import app.tivi.R
import app.tivi.epDetailsFirstAiredItem
import app.tivi.epDetailsSummary
import app.tivi.epDetailsWatchItem
import app.tivi.header
import app.tivi.ui.epoxy.TotalSpanOverride
import com.airbnb.epoxy.TypedEpoxyController

class EpisodeDetailsEpoxyController(
    private val callbacks: Callbacks
) : TypedEpoxyController<EpisodeDetailsViewState>() {

    interface Callbacks {
        // TODO
    }

    override fun buildModels(viewState: EpisodeDetailsViewState) {
        epDetailsSummary {
            id("episode_summary")
            episode(viewState.episode)
            spanSizeOverride(TotalSpanOverride)
        }
        epDetailsFirstAiredItem {
            id("first_aired")
            episode(viewState.episode)
            dateTimeFormatter(viewState.dateTimeFormatter)
            spanSizeOverride(TotalSpanOverride)
        }

        if (viewState.watches.isNotEmpty()) {
            header {
                id("watches_header")
                title(R.string.episode_watches)
                spanSizeOverride(TotalSpanOverride)
            }
            for (entry in viewState.watches) {
                epDetailsWatchItem {
                    id("watch_${entry.id}")
                    dateTimeFormatter(viewState.dateTimeFormatter)
                    watch(entry)
                    spanSizeOverride(TotalSpanOverride)
                }
            }
        }
    }
}