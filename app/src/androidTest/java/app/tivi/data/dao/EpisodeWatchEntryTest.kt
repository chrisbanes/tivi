/*
 * Copyright 2018 Google, Inc.
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

import android.database.sqlite.SQLiteConstraintException
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.utils.BaseDatabaseTest
import app.tivi.utils.deleteEpisodes
import app.tivi.utils.episodeWatch1
import app.tivi.utils.episodeWatch1Id
import app.tivi.utils.episodeWatch2PendingDelete
import app.tivi.utils.episodeWatch2PendingSend
import app.tivi.utils.insertEpisodes
import app.tivi.utils.insertSeason
import app.tivi.utils.insertShow
import app.tivi.utils.showId
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test

class EpisodeWatchEntryTest : BaseDatabaseTest() {
    private lateinit var episodeWatchEntryDao: EpisodeWatchEntryDao

    override fun setup() {
        super.setup()

        episodeWatchEntryDao = db.episodeWatchesDao()

        // We'll assume that there's a show, season and episodes in the db
        insertShow(db)
        insertSeason(db)
        insertEpisodes(db)
    }

    @Test
    fun insert() {
        episodeWatchEntryDao.insert(episodeWatch1)
        assertThat(episodeWatchEntryDao.entryWithId(episodeWatch1Id), `is`(episodeWatch1))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() {
        episodeWatchEntryDao.insert(episodeWatch1)
        // Make a copy with a null id
        val copy = episodeWatch1.copy(id = null)
        episodeWatchEntryDao.insert(copy)
    }

    @Test
    fun fetchEntries_WithPendingSendAction() {
        episodeWatchEntryDao.insertAll(episodeWatch1, episodeWatch2PendingSend)
        assertThat(episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId),
                `is`(listOf(episodeWatch2PendingSend))
        )
    }

    @Test
    fun fetchEntries_WithPendingDeleteAction() {
        episodeWatchEntryDao.insertAll(episodeWatch1, episodeWatch2PendingDelete)
        assertThat(episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId),
                `is`(listOf(episodeWatch2PendingDelete))
        )
    }

    @Test
    fun delete() {
        episodeWatchEntryDao.insert(episodeWatch1)
        episodeWatchEntryDao.delete(episodeWatch1)
        assertThat(episodeWatchEntryDao.entryWithId(episodeWatch1Id), `is`(nullValue()))
    }

    @Test
    fun deleteEpisode_deletesWatch() {
        episodeWatchEntryDao.insert(episodeWatch1)
        // Now delete episode
        deleteEpisodes(db)
        assertThat(episodeWatchEntryDao.entryWithId(episodeWatch1Id), `is`(nullValue()))
    }
}