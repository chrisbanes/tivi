// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.db

interface DatabaseTransactionRunner {
    operator fun <T> invoke(block: () -> T): T
}
