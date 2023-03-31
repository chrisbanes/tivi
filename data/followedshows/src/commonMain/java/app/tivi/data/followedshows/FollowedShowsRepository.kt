/*
 * Copyright 2023 Google LLC
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

package app.tivi.data.followedshows

import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PendingAction
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.data.util.ItemSyncerResult
import app.tivi.data.util.inPast
import app.tivi.data.util.syncerForEntity
import app.tivi.inject.ApplicationScope
import app.tivi.util.Logger
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class FollowedShowsRepository(
    private val followedShowsDao: FollowedShowsDao,
    private val followedShowsLastRequestStore: FollowedShowsLastRequestStore,
    private val dataSource: FollowedShowsDataSource,
    private val traktAuthRepository: TraktAuthRepository,
    private val logger: Logger,
    private val showDao: TiviShowDao,
) {
    private var traktListId: Int? = null

    private val syncer = syncerForEntity(
        entityDao = followedShowsDao,
        entityToKey = { it.traktId },
        mapper = { newEntity, currentEntity -> newEntity.copy(id = currentEntity?.id ?: 0) },
        logger = logger,
    )

    fun observeIsShowFollowed(showId: Long): Flow<Boolean> {
        return followedShowsDao.entryCountWithShowIdNotPendingDeleteObservable(showId)
            .map { it > 0 }
    }

    suspend fun isShowFollowed(showId: Long): Boolean {
        return followedShowsDao.entryCountWithShowId(showId) > 0
    }

    suspend fun getFollowedShows(): List<FollowedShowEntry> {
        return followedShowsDao.entries()
    }

    suspend fun needFollowedShowsSync(expiry: Instant = 3.hours.inPast): Boolean {
        return followedShowsLastRequestStore.isRequestBefore(expiry)
    }

    suspend fun addFollowedShow(showId: Long) {
        val entry = followedShowsDao.entryWithShowId(showId)

        logger.d("addFollowedShow. Current entry: %s", entry)

        if (entry == null || entry.pendingAction == PendingAction.DELETE) {
            // If we don't have an entry, or it is marked for deletion, lets update it to be uploaded
            val newEntry = FollowedShowEntry(
                id = entry?.id ?: 0,
                showId = showId,
                followedAt = entry?.followedAt ?: Clock.System.now(),
                pendingAction = PendingAction.UPLOAD,
            )
            val newEntryId = followedShowsDao.upsert(newEntry)

            logger.v("addFollowedShow. Entry saved with ID: %s - %s", newEntryId, newEntry)
        }
    }

    suspend fun removeFollowedShow(showId: Long) {
        // Update the followed show to be deleted
        followedShowsDao.entryWithShowId(showId)?.also {
            // Mark the show as pending deletion
            followedShowsDao.upsert(it.copy(pendingAction = PendingAction.DELETE))
        }
    }

    suspend fun syncFollowedShows(): ItemSyncerResult<FollowedShowEntry> {
        val listId = when (traktAuthRepository.state.value) {
            TraktAuthState.LOGGED_IN -> getFollowedTraktListId()
            else -> null
        }

        processPendingAdditions(listId)
        processPendingDelete(listId)

        return when {
            listId != null -> pullDownTraktFollowedList(listId)
            else -> ItemSyncerResult()
        }.also {
            followedShowsLastRequestStore.updateLastRequest()
        }
    }

    private suspend fun pullDownTraktFollowedList(
        listId: Int,
    ): ItemSyncerResult<FollowedShowEntry> {
        val response = dataSource.getListShows(listId)
        logger.d("pullDownTraktFollowedList. Response: %s", response)
        return response.map { (entry, show) ->
            // Grab the show id if it exists, or save the show and use it's generated ID
            val showId = showDao.getIdOrSavePlaceholder(show)
            // Create a followed show entry with the show id
            entry.copy(showId = showId)
        }.let { entries ->
            // Save the show entries
            syncer.sync(followedShowsDao.entries(), entries)
        }
    }

    private suspend fun processPendingAdditions(listId: Int?) {
        val pending = followedShowsDao.entriesWithSendPendingActions()
        logger.d("processPendingAdditions. listId: %s, Entries: %s", listId, pending)

        if (pending.isEmpty()) {
            return
        }

        if (listId != null && traktAuthRepository.state.value == TraktAuthState.LOGGED_IN) {
            val shows = pending.mapNotNull { showDao.getShowWithId(it.showId) }
            logger.v("processPendingAdditions. Entries mapped: %s", shows)

            val response = dataSource.addShowIdsToList(listId, shows)
            logger.v("processPendingAdditions. Trakt response: %s", response)

            // Now update the database
            followedShowsDao.updateEntriesToPendingAction(
                pending.map { it.id },
                PendingAction.NOTHING,
            )
        } else {
            // We're not logged in, so just update the database
            followedShowsDao.updateEntriesToPendingAction(
                pending.map { it.id },
                PendingAction.NOTHING,
            )
        }
    }

    private suspend fun processPendingDelete(listId: Int?) {
        val pending = followedShowsDao.entriesWithDeletePendingActions()
        logger.d("processPendingDelete. listId: %s, Entries: %s", listId, pending)

        if (pending.isEmpty()) {
            return
        }

        if (listId != null && traktAuthRepository.state.value == TraktAuthState.LOGGED_IN) {
            val shows = pending.mapNotNull { showDao.getShowWithId(it.showId) }
            logger.v("processPendingDelete. Entries mapped: %s", shows)

            val response = dataSource.removeShowIdsFromList(listId, shows)
            logger.v("processPendingDelete. Trakt response: %s", response)

            // Now update the database
            followedShowsDao.deleteWithIds(pending.map { it.id })
        } else {
            // We're not logged in, so just update the database
            followedShowsDao.deleteWithIds(pending.map { it.id })
        }
    }

    private suspend fun getFollowedTraktListId(): Int? {
        if (traktListId == null) {
            traktListId = try {
                dataSource.getFollowedListId().ids?.trakt
            } catch (e: Exception) {
                null
            }
        }
        return traktListId
    }
}
