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

package me.banes.chris.tivi.jobs

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.HistoryType
import com.uwetrottmann.trakt5.services.Users
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.data.DatabaseTransactionRunner
import me.banes.chris.tivi.data.daos.EpisodeWatchEntryDao
import me.banes.chris.tivi.data.daos.EpisodesDao
import me.banes.chris.tivi.data.daos.FollowedShowsDao
import me.banes.chris.tivi.data.entities.EpisodeWatchEntry
import me.banes.chris.tivi.extensions.fetchBodyWithRetry
import me.banes.chris.tivi.trakt.TraktAuthState
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class SyncShowWatchedProgress @Inject constructor(
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    private val episodesDao: EpisodesDao,
    private val followedShowsDao: FollowedShowsDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val traktManager: TraktManager,
    private val usersService: Provider<Users>,
    private val databaseTransactionRunner: DatabaseTransactionRunner
) : Job() {
    companion object {
        const val TAG = "sync-show-watched-episodes"
        private const val PARAM_FOLLOWED_ID = "show-id"

        fun buildRequest(followedId: Long): JobRequest.Builder {
            return JobRequest.Builder(TAG).addExtras(
                    PersistableBundleCompat().apply {
                        putLong(PARAM_FOLLOWED_ID, followedId)
                    }
            )
        }
    }

    override fun onRunJob(params: Params): Result {
        val followedId = params.extras.getLong(PARAM_FOLLOWED_ID, -1)
        Timber.d("$TAG job running for id: $followedId")

        val authState = traktManager.state.blockingFirst()
        if (authState == TraktAuthState.LOGGED_IN) {
            return runBlocking {
                sync(followedId)
                Result.SUCCESS
            }
        }

        return Result.FAILURE
    }

    private suspend fun sync(followedId: Long) {
        val followedEntry = withContext(dispatchers.database) { followedShowsDao.entryWithId(followedId) }
            ?: throw IllegalArgumentException("Followed entry with id: $followedId does not exist")
        val show = followedEntry.show!!

        // TODO fetch all un-synced watches from DB and send to Trakt

        // Fetch watched progress for show
        val watchedProgress = withContext(dispatchers.network) {
            // TODO use start and end dates
            usersService.get().history(UserSlug.ME, HistoryType.SHOWS, show.traktId!!,
                    0, 10000, Extended.NOSEASONS, null, null).fetchBodyWithRetry()
        }

        withContext(dispatchers.database) {
            databaseTransactionRunner.runInTransaction {
                // Probably want to delete all current

                watchedProgress.forEach { historyEntry ->
                    // We only care about episode watch entries
                    val traktEpisode = historyEntry.episode ?: return@forEach

                    // Fetch our entity for the history entry
                    val currentEntry = episodeWatchEntryDao.entryWithTraktId(historyEntry.id)

                    // Now fetch the episode entity from the database
                    val traktId = traktEpisode.ids.trakt
                    val episode = episodesDao.episodeWithTraktId(traktId)
                            ?: throw IllegalArgumentException("Episode with id $traktId does not exist.")

                    if (currentEntry != null) {
                        // TODO update?
                    } else {
                        val entry = EpisodeWatchEntry(
                                episodeId = episode.id!!,
                                traktId = historyEntry.id,
                                watchedAt = historyEntry.watched_at,
                                source = EpisodeWatchEntry.SOURCE_TRAKT
                        )
                        episodeWatchEntryDao.insert(entry)
                    }
                }
            }
        }
    }
}