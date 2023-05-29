// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

data class PopularShowEntry(
    override val id: Long = 0,
    override val showId: Long,
    override val page: Int,
    val pageOrder: Int,
) : PaginatedEntry
