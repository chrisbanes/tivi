// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import kotlinx.datetime.Instant

data class EpisodeWatchEntry(
    override val id: Long = 0,
    val episodeId: Long,
    val traktId: Long? = null,
    val watchedAt: Instant,
    val pendingAction: PendingAction = PendingAction.NOTHING,
) : TiviEntity
