// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

interface Entry : TiviEntity {
    val showId: Long
}

interface MultipleEntry : Entry {
    val otherShowId: Long
}

interface PaginatedEntry : Entry {
    val page: Int
}
