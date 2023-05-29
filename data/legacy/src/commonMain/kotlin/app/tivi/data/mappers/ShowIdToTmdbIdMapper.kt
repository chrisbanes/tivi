// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.tivi.data.daos.TiviShowDao
import me.tatarka.inject.annotations.Inject

@Inject
class ShowIdToTmdbIdMapper(
    private val showDao: TiviShowDao,
) : Mapper<Long, Int?> {
    override fun map(from: Long) = showDao.getTmdbIdForShowId(from)
}
