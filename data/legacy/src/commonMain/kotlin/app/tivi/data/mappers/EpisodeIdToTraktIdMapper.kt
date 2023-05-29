// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.tivi.data.daos.EpisodesDao
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeIdToTraktIdMapper(
    private val dao: EpisodesDao,
) : Mapper<Long, Int> {
    override fun map(from: Long): Int {
        return dao.episodeTraktIdForId(from)
            ?: throw IllegalArgumentException("Episode with id $from does not exist")
    }
}
