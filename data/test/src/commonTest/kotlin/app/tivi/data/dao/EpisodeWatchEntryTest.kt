// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.dao

import app.tivi.data.DatabaseTest
import app.tivi.data.TestApplicationComponent
import app.tivi.data.create
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.insert
import app.tivi.utils.episodeWatch2PendingDelete
import app.tivi.utils.episodeWatch2PendingSend
import app.tivi.utils.s1
import app.tivi.utils.s1_episodes
import app.tivi.utils.s1e1
import app.tivi.utils.s1e1w
import app.tivi.utils.s1e1w_id
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

class EpisodeWatchEntryTest : DatabaseTest() {
    private lateinit var showsDao: TiviShowDao
    private lateinit var episodesDao: EpisodesDao
    private lateinit var seasonsDao: SeasonsDao
    private lateinit var episodeWatchEntryDao: EpisodeWatchEntryDao

    @BeforeTest
    fun setup() {
        val component = EpisodeWatchEntryTestComponent::class.create(applicationComponent)
        showsDao = component.showsDao
        seasonsDao = component.seasonsDao
        episodesDao = component.episodesDao
        episodeWatchEntryDao = component.episodeWatchEntryDao

        // We'll assume that there's a show, season and s1_episodes in the db
        showsDao.insert(show)
        seasonsDao.insert(s1)
        episodesDao.insert(s1_episodes)
    }

    @Test
    fun insert() {
        episodeWatchEntryDao.insert(s1e1w)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id)).isEqualTo(s1e1w)
    }

    @Test
    fun insert_withSameTraktId() {
        episodeWatchEntryDao.insert(s1e1w)

        assertFails {
            // Make a copy with a 0 id
            val copy = s1e1w.copy(id = 0)
            episodeWatchEntryDao.insert(copy)
        }
    }

    @Test
    fun fetchEntries_WithPendingSendAction() {
        episodeWatchEntryDao.insert(s1e1w, episodeWatch2PendingSend)
        assertThat(episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId))
            .containsExactly(episodeWatch2PendingSend)
    }

    @Test
    fun fetchEntries_WithPendingDeleteAction() {
        episodeWatchEntryDao.insert(s1e1w, episodeWatch2PendingDelete)
        assertThat(episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId))
            .containsExactly(episodeWatch2PendingDelete)
    }

    @Test
    fun delete() {
        episodeWatchEntryDao.insert(s1e1w)
        episodeWatchEntryDao.deleteEntity(s1e1w)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id)).isNull()
    }

    @Test
    fun deleteEpisode_deletesWatch() {
        episodeWatchEntryDao.insert(s1e1w)
        // Now delete episode
        episodesDao.deleteEntity(s1e1)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id)).isNull()
    }
}

@Component
abstract class EpisodeWatchEntryTestComponent(
    @Component val applicationComponent: TestApplicationComponent,
) {
    abstract val showsDao: TiviShowDao
    abstract val episodesDao: EpisodesDao
    abstract val seasonsDao: SeasonsDao
    abstract val episodeWatchEntryDao: EpisodeWatchEntryDao
}
