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
import timber.log.Timber
import javax.inject.Inject

class AddShowToMyShows @Inject constructor() : Job() {
    companion object {
        const val TAG = "AddShowToMyShows"

        private const val PARAM_TRAKT_ID = "_trakt-id"

        fun schedule(traktId: String) {
            JobRequest.Builder(TAG)
                    .addExtras(
                            PersistableBundleCompat().apply {
                                putString(PARAM_TRAKT_ID, traktId)
                            }
                    )
                    .startNow()
                    .build()
                    .scheduleAsync()
        }
    }

    override fun onRunJob(params: Params): Result {
        Timber.d("AddShowToMyShows job running!!")

        return Result.SUCCESS
    }
}