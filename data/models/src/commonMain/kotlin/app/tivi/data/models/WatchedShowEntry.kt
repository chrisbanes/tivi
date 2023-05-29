// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import kotlinx.datetime.Instant

data class WatchedShowEntry(
    override val id: Long = 0,
    override val showId: Long,
    val lastWatched: Instant,
    val lastUpdated: Instant,
) : Entry
