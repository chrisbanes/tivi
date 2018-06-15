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

import app.tivi.interactors.SyncAllFollowedShowsInteractor
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import kotlinx.coroutines.experimental.runBlocking
import javax.inject.Inject

class SyncAllFollowedShows @Inject constructor(
    private val syncAllFollowedShows: SyncAllFollowedShowsInteractor
) : Job() {
    companion object {
        const val TAG = "sync-all-followed-shows"

        fun buildRequest(): JobRequest.Builder {
            return JobRequest.Builder(TAG)
        }
    }

    override fun onRunJob(params: Params): Result {
        runBlocking {
            syncAllFollowedShows(Unit)
        }
        return Result.SUCCESS
    }
}