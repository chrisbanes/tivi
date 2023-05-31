// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.dao

import app.tivi.data.DatabaseTest
import app.tivi.data.TestApplicationComponent
import app.tivi.data.create
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.utils.s1
import app.tivi.utils.s1e1
import app.tivi.utils.show
import app.tivi.utils.showId
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import me.tatarka.inject.annotations.Component

class EpisodesTest : DatabaseTest() {
    private lateinit var showsDao: TiviShowDao
    private lateinit var episodeDao: EpisodesDao
    private lateinit var seasonsDao: SeasonsDao

    @BeforeTest
    fun setup() {
        val component = EpisodesTestComponent::class.create(applicationComponent)
        showsDao = component.showsDao
        seasonsDao = component.seasonsDao
        episodeDao = component.episodeDao

        // We'll assume that there's a show and season in the db
        showsDao.insert(show)
        seasonsDao.insert(s1)
    }

    @Test
    fun insert() {
        episodeDao.insert(s1e1)
        assertThat(episodeDao.episodeWithId(s1e1.id)).isEqualTo(s1e1)
    }

    @Test
    fun insert_withSameTraktId() {
        episodeDao.insert(s1e1)
        assertFails {
            // Make a copy with a 0 id
            val copy = s1e1.copy(id = 0)
            episodeDao.insert(copy)
        }
    }

    @Test
    fun delete() {
        episodeDao.insert(s1e1)
        episodeDao.deleteEntity(s1e1)
        assertThat(episodeDao.episodeWithId(s1e1.id)).isNull()
    }

    @Test
    fun deleteSeason_deletesEpisode() {
        episodeDao.insert(s1e1)
        // Now delete season
        seasonsDao.deleteEntity(s1)
        assertThat(episodeDao.episodeWithId(s1e1.id)).isNull()
    }

    @Test
    fun showIdForEpisodeId() {
        episodeDao.insert(s1e1)
        assertThat(episodeDao.showIdForEpisodeId(s1e1.id)).isEqualTo(showId)
    }
}

@Component
abstract class EpisodesTestComponent(
    @Component val applicationComponent: TestApplicationComponent,
) {
    abstract val showsDao: TiviShowDao
    abstract val episodeDao: EpisodesDao
    abstract val seasonsDao: SeasonsDao
}
