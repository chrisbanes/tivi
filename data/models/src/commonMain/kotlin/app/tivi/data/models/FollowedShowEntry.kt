// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import kotlinx.datetime.Instant

data class FollowedShowEntry(
    override val id: Long = 0,
    override val showId: Long,
    val followedAt: Instant? = null,
    val pendingAction: PendingAction = PendingAction.NOTHING,
    val traktId: Long? = null,
) : Entry
