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

package app.tivi.data.compoundmodels

import app.tivi.data.models.Episode
import app.tivi.data.models.Season

data class SeasonWithEpisodesAndWatches(
    val season: Season,
    val episodes: List<EpisodeWithWatches> = emptyList(),
) {
    val numberAiredToWatch: Int by lazy {
        episodes.count { !it.isWatched && it.episode.hasAired }
    }

    val numberWatched: Int by lazy {
        episodes.count { it.isWatched }
    }

    val numberToAir: Int by lazy {
        episodes.size - numberAired
    }

    val numberAired: Int by lazy {
        episodes.count { it.episode.hasAired }
    }

    val nextToAir: Episode? by lazy {
        episodes.firstOrNull {
            it.episode.let { ep -> !ep.hasAired && ep.firstAired != null }
        }?.episode
    }
}
