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

import app.tivi.data.daos.FollowedShowsDao
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class SyncShowWatchedProgress @Inject constructor(
    private val syncer: TraktEpisodeWatchSyncer,
    private val followedShowsDao: FollowedShowsDao,
    private val dispatchers: AppCoroutineDispatchers,
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

        return runBlocking {
            val followedEntry = withContext(dispatchers.database) {
                followedShowsDao.entryWithShowId(showId)
            } ?: throw IllegalArgumentException("Followed entry with id: $showId does not exist")
            val show = followedEntry.show!!

            syncer.sync(show.id!!)

            Result.SUCCESS
        }
    }
}