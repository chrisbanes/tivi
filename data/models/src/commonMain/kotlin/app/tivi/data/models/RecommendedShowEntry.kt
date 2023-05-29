// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

data class RecommendedShowEntry(
    override val id: Long = 0,
    override val showId: Long,
    override val page: Int,
) : PaginatedEntry
