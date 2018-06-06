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

package app.tivi.data

import android.database.sqlite.SQLiteConstraintException
import app.tivi.data.daos.EpisodeWatchEntryDao
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test

class EpisodeWatchEntryTest : BaseTest() {
    private lateinit var episodeWatchEntryDao: EpisodeWatchEntryDao

    override fun setup() {
        super.setup()

        episodeWatchEntryDao = db.episodeWatchesDao()

        // We'll assume that there's a show, season and episodes in the db
        SampleData.insertShow(db)
        SampleData.insertSeason(db)
        SampleData.insertEpisodes(db)
    }

    @Test
    fun insert() {
        episodeWatchEntryDao.insert(SampleData.episodeWatch)
        assertThat(episodeWatchEntryDao.entryWithId(SampleData.episodeWatchId), `is`(SampleData.episodeWatch))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_withSameTraktId() {
        episodeWatchEntryDao.insert(SampleData.episodeWatch)
        // Make a copy with a null id
        val copy = SampleData.episodeWatch.copy(id = null)
        episodeWatchEntryDao.insert(copy)
    }

    @Test
    fun fetchEntries_WithPendingSendAction() {
        episodeWatchEntryDao.insert(SampleData.episodeWatch)
        episodeWatchEntryDao.insert(SampleData.episodeWatchPendingDelete)
        episodeWatchEntryDao.insert(SampleData.episodeWatchPendingSend)
        assertThat(
                episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(SampleData.showId),
                `is`(listOf(SampleData.episodeWatchPendingSend))
        )
    }

    @Test
    fun fetchEntries_WithPendingDeleteAction() {
        episodeWatchEntryDao.insert(SampleData.episodeWatch)
        episodeWatchEntryDao.insert(SampleData.episodeWatchPendingDelete)
        episodeWatchEntryDao.insert(SampleData.episodeWatchPendingSend)
        assertThat(
                episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(SampleData.showId),
                `is`(listOf(SampleData.episodeWatchPendingDelete))
        )
    }

    @Test
    fun delete() {
        episodeWatchEntryDao.insert(SampleData.episodeWatch)
        episodeWatchEntryDao.delete(SampleData.episodeWatch)
        assertThat(episodeWatchEntryDao.entryWithId(SampleData.episodeWatchId), `is`(nullValue()))
    }

    @Test
    fun deleteEpisode_deletesWatch() {
        episodeWatchEntryDao.insert(SampleData.episodeWatch)
        // Now delete episode
        SampleData.deleteEpisodes(db)
        assertThat(episodeWatchEntryDao.entryWithId(SampleData.episodeWatchId), `is`(nullValue()))
    }
}