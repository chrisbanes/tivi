// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

enum class ShowStatus(val storageKey: String) {
    ENDED("ended"),
    RETURNING("returning"),
    CANCELED("canceled"),
    IN_PRODUCTION("inproduction"),
    PLANNED("planned"),
}
