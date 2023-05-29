// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

data class RelatedShowEntry(
    override val id: Long = 0,
    override val showId: Long,
    override val otherShowId: Long,
    val orderIndex: Int,
) : MultipleEntry
