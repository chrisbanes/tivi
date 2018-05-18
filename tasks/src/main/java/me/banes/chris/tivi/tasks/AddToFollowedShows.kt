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

package me.banes.chris.tivi.tasks

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.SeasonFetcher
import me.banes.chris.tivi.actions.ShowTasks
import me.banes.chris.tivi.data.daos.FollowedShowsDao
import me.banes.chris.tivi.data.entities.FollowedShowEntry
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import timber.log.Timber
import javax.inject.Inject

class AddToFollowedShows @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val followedShowsDao: FollowedShowsDao,
    private val seasonFetcher: SeasonFetcher,
    private val showTasks: ShowTasks
) : Job() {

    companion object {
        const val TAG = "myshows-add"
        private const val PARAM_SHOW_ID = "show-id"

        fun buildRequest(showId: Long): JobRequest.Builder {
            return JobRequest.Builder(TAG).addExtras(
                    PersistableBundleCompat().apply {
                        putLong(PARAM_SHOW_ID, showId)
                    }
            )
        }
    }

    override fun onRunJob(params: Params): Result {
        val showId = params.extras.getLong(PARAM_SHOW_ID, -1)

        Timber.d("$TAG job running for id: $showId")

        return runBlocking {
            val entryId = withContext(dispatchers.database) {
                followedShowsDao.insert(FollowedShowEntry(showId = showId))
            }

            // Now refresh seasons
            seasonFetcher.load(showId)
            // And sync watched episodes
            showTasks.syncShowWatchedEpisodes(entryId)

            Result.SUCCESS
        }
    }
}