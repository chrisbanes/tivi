// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.tivi.data.daos.SeasonsDao
import me.tatarka.inject.annotations.Inject

@Inject
class SeasonIdToTraktIdMapper(
    private val dao: SeasonsDao,
) : Mapper<Long, Int> {
    override fun map(from: Long): Int {
        return dao.traktIdForId(from)
            ?: throw IllegalArgumentException("Trakt Id for season id $from does not exist")
    }
}
