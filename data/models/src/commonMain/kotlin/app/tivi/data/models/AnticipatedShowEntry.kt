// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

data class AnticipatedShowEntry(
  override val id: Long = 0,
  override val showId: Long,
  override val page: Int,
  val pageOrder: Int,
) : PaginatedEntry
