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

package app.tivi.tasks

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.sync.Syncer
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.uwetrottmann.trakt5.entities.HistoryEntry
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.HistoryType
import com.uwetrottmann.trakt5.services.Users
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject
import javax.inject.Provider

class SyncShowWatchedProgress @Inject constructor(
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    private val episodesDao: EpisodesDao,
    private val followedShowsDao: FollowedShowsDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val traktManager: TraktManager,
    private val usersService: Provider<Users>,
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    private val logger: Logger
) : Job() {
    companion object {
        const val TAG = "sync-show-watched-episodes"
        private const val PARAM_SHOW_ID = "show-id"

        fun buildRequest(followedId: Long): JobRequest.Builder {
            return JobRequest.Builder(TAG).addExtras(
                    PersistableBundleCompat().apply {
                        putLong(PARAM_SHOW_ID, followedId)
                    }
            )
        }
    }

    override fun onRunJob(params: Params): Result {
        val showId = params.extras.getLong(PARAM_SHOW_ID, -1)
        logger.d("$TAG job running for show id: $showId")

        val authState = traktManager.state.blockingFirst()
        if (authState == TraktAuthState.LOGGED_IN) {
            return runBlocking {
                sync(showId)
                Result.SUCCESS
            }
        }

        return Result.FAILURE
    }

    private suspend fun sync(showId: Long) {
        val followedEntry = withContext(dispatchers.database) { followedShowsDao.entryWithShowId(showId) }
                ?: throw IllegalArgumentException("Followed entry with id: $showId does not exist")
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
                val syncer = Syncer(
                        episodeWatchEntryDao::entryTraktIds,
                        episodeWatchEntryDao::entryWithTraktId,
                        episodeWatchEntryDao::deleteWithTraktId,
                        episodeWatchEntryDao::insert,
                        episodeWatchEntryDao::update,
                        HistoryEntry::id,
                        ::mapEntry,
                        { old, new -> new.copy(id = old.id) },
                        logger
                )
                syncer.sync(watchedProgress)
            }
        }
    }

    private fun mapEntry(historyEntry: HistoryEntry): EpisodeWatchEntry {
        val episodeTraktId = historyEntry.episode.ids.trakt
        val episode = episodesDao.episodeWithTraktId(episodeTraktId)
                ?: throw IllegalArgumentException("Episode with id $episodeTraktId does not exist.")

        return EpisodeWatchEntry(
                episodeId = episode.id!!,
                traktId = historyEntry.id,
                watchedAt = historyEntry.watched_at,
                source = EpisodeWatchEntry.SOURCE_TRAKT
        )
    }
}