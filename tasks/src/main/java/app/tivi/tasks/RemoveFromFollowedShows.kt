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

import app.tivi.interactors.UnfollowShowInteractor
import app.tivi.util.Logger
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import kotlinx.coroutines.experimental.runBlocking
import javax.inject.Inject

class RemoveFromFollowedShows @Inject constructor(
    private val unfollowShowCall: UnfollowShowInteractor,
    private val logger: Logger
) : Job() {

    companion object {
        const val TAG = "myshows-remove"

        private const val PARAM_SHOW_ID = "show-id"

        fun buildRequest(showId: Long): JobRequest.Builder {
            return JobRequest.Builder(TAG).addExtras(
                    PersistableBundleCompat().apply {
                        putLong(PARAM_SHOW_ID, showId)
                    })
        }
    }

    override fun onRunJob(params: Params): Result {
        val showId = params.extras.getLong(PARAM_SHOW_ID, -1)
        logger.d("$TAG job running for id: $showId")

        return runBlocking {
            unfollowShowCall.doWork(showId)
            Result.SUCCESS
        }
    }
}