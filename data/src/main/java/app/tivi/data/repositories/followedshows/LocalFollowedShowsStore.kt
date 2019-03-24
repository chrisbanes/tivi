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

package app.tivi.data.repositories.followedshows

import androidx.paging.DataSource
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Request
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.data.syncers.syncerForEntity
import io.reactivex.Flowable
import org.threeten.bp.temporal.TemporalAmount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFollowedShowsStore @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val entityInserter: EntityInserter,
    private val followedShowsDao: FollowedShowsDao,
    private val showDao: TiviShowDao,
    private val lastRequestDao: LastRequestDao
) {
    var traktListId: Int? = null

    private val syncer = syncerForEntity(
            followedShowsDao,
            { showDao.getTraktIdForShowId(it.showId)!! },
            { entity, id -> entity.copy(id = id ?: 0) }
    )

    suspend fun getEntryForShowId(showId: Long): FollowedShowEntry? = followedShowsDao.entryWithShowId(showId)

    suspend fun getEntries(): List<FollowedShowEntry> = followedShowsDao.entries()

    suspend fun getEntriesWithAddAction() = followedShowsDao.entriesWithSendPendingActions()

    suspend fun getEntriesWithDeleteAction() = followedShowsDao.entriesWithDeletePendingActions()

    suspend fun updateEntriesWithAction(ids: List<Long>, action: PendingAction): Int {
        return followedShowsDao.updateEntriesToPendingAction(ids, action.value)
    }

    suspend fun deleteEntriesInIds(ids: List<Long>) = followedShowsDao.deleteWithIds(ids)

    fun observeForPaging(): DataSource.Factory<Int, FollowedShowEntryWithShow> = followedShowsDao.entriesDataSource()

    fun observeIsShowFollowed(showId: Long): Flowable<Boolean> {
        return followedShowsDao.entryCountWithShowIdNotPendingDeleteFlowable(showId)
                .map { it > 0 }
    }

    suspend fun isShowFollowed(showId: Long) = followedShowsDao.entryCountWithShowId(showId) > 0

    suspend fun sync(entities: List<FollowedShowEntry>) = transactionRunner {
        syncer.sync(followedShowsDao.entries(), entities)
    }

    suspend fun updateLastFollowedShowsSync() {
        lastRequestDao.updateLastRequest(Request.FOLLOWED_SHOWS, 0)
    }

    suspend fun isLastFollowedShowsSyncBefore(threshold: TemporalAmount): Boolean {
        return lastRequestDao.isRequestBefore(Request.FOLLOWED_SHOWS, 0, threshold)
    }

    suspend fun save(entry: FollowedShowEntry) {
        entityInserter.insertOrUpdate(followedShowsDao, entry)
    }
}