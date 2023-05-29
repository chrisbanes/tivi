// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import app.tivi.data.db.DatabaseTransactionRunner
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightTransactionRunner(private val db: Database) : DatabaseTransactionRunner {
    override fun <T> invoke(block: () -> T): T {
        return db.transactionWithResult {
            block()
        }
    }
}
