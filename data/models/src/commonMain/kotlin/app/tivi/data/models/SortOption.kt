// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

enum class SortOption(val sqlValue: String) {
    LAST_WATCHED("last_watched"),
    AIR_DATE("recently_aired"),
    ALPHABETICAL("alpha"),
    DATE_ADDED("added"),
}
