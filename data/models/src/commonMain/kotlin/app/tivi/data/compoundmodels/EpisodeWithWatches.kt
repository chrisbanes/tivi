// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
