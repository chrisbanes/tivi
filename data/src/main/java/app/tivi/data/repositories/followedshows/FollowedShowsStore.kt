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

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.data.resultentities.FollowedShowsRawSection
import app.tivi.data.resultentities.FollowedShowsSection
import app.tivi.data.syncers.syncerForEntity
import app.tivi.data.views.FollowedShowsWatchStats
import app.tivi.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowedShowsStore @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val followedShowsDao: FollowedShowsDao,
    logger: Logger
) {
    var traktListId: Int? = null

    private val syncer = syncerForEntity(
        entityDao = followedShowsDao,
        entityToKey = { it.traktId },
        mapper = { entity, id -> entity.copy(id = id ?: 0) },
        logger = logger
    )

    suspend fun getEntryForShowId(showId: Long): FollowedShowEntry? = followedShowsDao.entryWithShowId(showId)

    suspend fun getEntries(): List<FollowedShowEntry> = followedShowsDao.entries()

    suspend fun getEntriesWithAddAction() = followedShowsDao.entriesWithSendPendingActions()

    suspend fun getEntriesWithDeleteAction() = followedShowsDao.entriesWithDeletePendingActions()

    suspend fun updateEntriesWithAction(ids: List<Long>, action: PendingAction): Int {
        return followedShowsDao.updateEntriesToPendingAction(ids, action.value)
    }

    suspend fun deleteEntriesInIds(ids: List<Long>) = followedShowsDao.deleteWithIds(ids)

    fun observeForPaging(
        sort: SortOption,
        filter: String?
    ): List<FollowedShowsRawSection> = when (sort) {
        SortOption.SUPER_SORT -> {
            followedShowsDao.pagedListSuperSort(filter)
                .let { FollowedShowsRawSection("Shows", it) }
                .let { listOf(it) }
        }
        SortOption.LAST_WATCHED -> {
            followedShowsDao.pagedListLastWatched(filter)
                .let { FollowedShowsRawSection("Shows", it) }
                .let { listOf(it) }
        }
        SortOption.ALPHABETICAL -> {
            followedShowsDao.pagedListAlpha(filter)
                .let { FollowedShowsRawSection("Shows", it) }
                .let { listOf(it) }
        }
        SortOption.DATE_ADDED -> {
            followedShowsDao.pagedListAdded(filter)
                .let { FollowedShowsRawSection("Shows", it) }
                .let { listOf(it) }
        }
    }

    fun observeIsShowFollowed(showId: Long): Flow<Boolean> {
        return followedShowsDao.entryCountWithShowIdNotPendingDeleteObservable(showId)
            .map { it > 0 }
    }

    fun observeNextShowToWatch(): Flow<FollowedShowEntryWithShow?> {
        return followedShowsDao.observeNextShowToWatch()
    }

    fun observeShowViewStats(showId: Long): Flow<FollowedShowsWatchStats?> {
        return followedShowsDao.entryShowViewStats(showId)
    }

    suspend fun isShowFollowed(showId: Long) = followedShowsDao.entryCountWithShowId(showId) > 0

    suspend fun sync(entities: List<FollowedShowEntry>) = transactionRunner {
        syncer.sync(followedShowsDao.entries(), entities)
    }

    suspend fun save(entry: FollowedShowEntry) = followedShowsDao.insertOrUpdate(entry)
}
