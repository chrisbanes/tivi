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
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.PendingAction

data class EpisodeWithWatches(
    val episode: Episode,
    val watches: List<EpisodeWatchEntry>,
) {
    val hasWatches by lazy { watches.isNotEmpty() }

    val isWatched by lazy {
        watches.any { it.pendingAction != PendingAction.DELETE }
    }

    val hasPending by lazy {
        watches.any { it.pendingAction != PendingAction.NOTHING }
    }

    val onlyPendingDeletes by lazy {
        watches.all { it.pendingAction == PendingAction.DELETE }
    }

    val hasAired: Boolean get() = episode.hasAired
}
