/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.data.dao

import app.tivi.data.DatabaseTest
import app.tivi.data.TestApplicationComponent
import app.tivi.data.create
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.upsertAll
import app.tivi.utils.episodeWatch2PendingDelete
import app.tivi.utils.episodeWatch2PendingSend
import app.tivi.utils.s1
import app.tivi.utils.s1_episodes
import app.tivi.utils.s1e1
import app.tivi.utils.s1e1w
import app.tivi.utils.s1e1w_id
import app.tivi.utils.show
import app.tivi.utils.showId
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.tatarka.inject.annotations.Component
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class EpisodeWatchEntryTest : DatabaseTest() {
    private lateinit var showsDao: TiviShowDao
    private lateinit var episodesDao: EpisodesDao
    private lateinit var seasonsDao: SeasonsDao
    private lateinit var episodeWatchEntryDao: EpisodeWatchEntryDao

    @Before
    fun setup() {
        val component = EpisodeWatchEntryTestComponent::class.create()
        showsDao = component.showsDao
        seasonsDao = component.seasonsDao
        episodesDao = component.episodesDao
        episodeWatchEntryDao = component.episodeWatchEntryDao

        runBlocking {
            // We'll assume that there's a show, season and s1_episodes in the db
            showsDao.upsert(show)
            seasonsDao.upsert(s1)
            episodesDao.upsertAll(s1_episodes)
        }
    }

    @Test
    fun insert() = runTest {
        episodeWatchEntryDao.upsert(s1e1w)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id), `is`(s1e1w))
    }

    @Test(expected = Exception::class) // Can't be more granular
    fun insert_withSameTraktId() = runTest {
        episodeWatchEntryDao.upsert(s1e1w)
        // Make a copy with a 0 id
        val copy = s1e1w.copy(id = 0)
        episodeWatchEntryDao.upsert(copy)
    }

    @Test
    fun fetchEntries_WithPendingSendAction() = runTest {
        episodeWatchEntryDao.upsertAll(s1e1w, episodeWatch2PendingSend)
        assertThat(
            episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId),
            `is`(listOf(episodeWatch2PendingSend)),
        )
    }

    @Test
    fun fetchEntries_WithPendingDeleteAction() = runTest {
        episodeWatchEntryDao.upsertAll(s1e1w, episodeWatch2PendingDelete)
        assertThat(
            episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId),
            `is`(listOf(episodeWatch2PendingDelete)),
        )
    }

    @Test
    fun delete() = runTest {
        episodeWatchEntryDao.upsert(s1e1w)
        episodeWatchEntryDao.deleteEntity(s1e1w)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id), `is`(nullValue()))
    }

    @Test
    fun deleteEpisode_deletesWatch() = runTest {
        episodeWatchEntryDao.upsert(s1e1w)
        // Now delete episode
        episodesDao.deleteEntity(s1e1)
        assertThat(episodeWatchEntryDao.entryWithId(s1e1w_id), `is`(nullValue()))
    }
}

@Component
abstract class EpisodeWatchEntryTestComponent(
    @Component val testApplicationComponent: TestApplicationComponent =
        TestApplicationComponent::class.create(),
) {
    abstract val showsDao: TiviShowDao
    abstract val episodesDao: EpisodesDao
    abstract val seasonsDao: SeasonsDao
    abstract val episodeWatchEntryDao: EpisodeWatchEntryDao
}
