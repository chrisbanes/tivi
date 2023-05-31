// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.dao

import app.tivi.data.DatabaseTest
import app.tivi.data.TestApplicationComponent
import app.tivi.data.create
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.utils.s0
import app.tivi.utils.s1
import app.tivi.utils.s1_id
import app.tivi.utils.s2
import app.tivi.utils.show
import app.tivi.utils.showId
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import me.tatarka.inject.annotations.Component

class SeasonsTest : DatabaseTest() {
    private lateinit var showsDao: TiviShowDao
    private lateinit var seasonsDao: SeasonsDao

    @BeforeTest
    fun setup() {
        val component = SeasonsTestComponent::class.create(applicationComponent)
        showsDao = component.showsDao
        seasonsDao = component.seasonsDao

        // We'll assume that there's a show in the db
        showsDao.insert(show)
    }

    @Test
    fun insertSeason() {
        seasonsDao.insert(s1)

        assertThat(seasonsDao.seasonWithId(s1_id)).isEqualTo(s1)
    }

    @Test
    fun insert_withSameTraktId() {
        seasonsDao.insert(s1)

        assertFails {
            // Make a copy with a 0 id
            val copy = s1.copy(id = 0)
            seasonsDao.insert(copy)
        }
    }

    @Test
    fun specialsOrder() {
        seasonsDao.insert(s0)
        seasonsDao.insert(s1)
        seasonsDao.insert(s2)

        // Specials should always be last
        assertThat(seasonsDao.seasonsForShowId(showId)).containsExactly(s1, s2, s0)
    }

    @Test
    fun deleteSeason() {
        seasonsDao.insert(s1)
        seasonsDao.deleteEntity(s1)

        assertThat(seasonsDao.seasonWithId(s1_id)).isNull()
    }

    @Test
    fun deleteShow_deletesSeason() {
        seasonsDao.insert(s1)
        // Now delete show
        showsDao.deleteEntity(show)

        assertThat(seasonsDao.seasonWithId(s1_id)).isNull()
    }
}

@Component
abstract class SeasonsTestComponent(
    @Component val applicationComponent: TestApplicationComponent,
) {
    abstract val showsDao: TiviShowDao
    abstract val seasonsDao: SeasonsDao
}
