// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.db.DatabaseTransactionRunner
import me.tatarka.inject.annotations.Inject

@Inject
class ShowIdToTraktOrImdbIdMapper(
    private val showDao: TiviShowDao,
    private val transactionRunner: DatabaseTransactionRunner,
) : Mapper<Long, String?> {
    override fun map(from: Long): String? = transactionRunner {
        showDao.getTraktIdForShowId(from)?.toString()
            ?: showDao.getImdbIdForShowId(from)
    }
}
