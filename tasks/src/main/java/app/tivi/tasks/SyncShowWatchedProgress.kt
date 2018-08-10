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

import androidx.work.Data
import androidx.work.Worker
import app.tivi.interactors.UpdateFollowedShowSeasonData
import app.tivi.util.Logger
import kotlinx.coroutines.experimental.runBlocking
import javax.inject.Inject

class SyncShowWatchedProgress : Worker() {
    companion object {
        const val TAG = "sync-show-watched-episodes"
        private const val PARAM_SHOW_ID = "show-id"

        fun buildData(showId: Long) = Data.Builder()
                .putLong(PARAM_SHOW_ID, showId)
                .build()
    }

    @Inject lateinit var updateShowSeasonsAndWatchedProgress: UpdateFollowedShowSeasonData
    @Inject lateinit var logger: Logger

    override fun doWork(): Result {
        AndroidWorkerInjector.inject(this)

        val showId = inputData.getLong(PARAM_SHOW_ID, -1)
        logger.d("$TAG worker running for show id: $showId")

        return runBlocking {
            updateShowSeasonsAndWatchedProgress(UpdateFollowedShowSeasonData.Params(showId, true))
            Result.SUCCESS
        }
    }
}