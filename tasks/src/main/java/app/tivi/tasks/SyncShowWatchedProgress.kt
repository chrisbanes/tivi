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

package app.tivi.tasks

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import app.tivi.domain.interactors.UpdateShowSeasonData
import app.tivi.util.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncShowWatchedProgress @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val updateShowSeasonData: UpdateShowSeasonData,
    private val logger: Logger
) : CoroutineWorker(context, params) {
    companion object {
        const val TAG = "sync-show-watched-episodes"
        private const val PARAM_SHOW_ID = "show-id"

        fun buildData(showId: Long) = Data.Builder()
            .putLong(PARAM_SHOW_ID, showId)
            .build()
    }

    override suspend fun doWork(): Result {
        val showId = inputData.getLong(PARAM_SHOW_ID, -1)
        logger.d("$TAG worker running for show id: $showId")

        updateShowSeasonData.executeSync(UpdateShowSeasonData.Params(showId, true))

        return Result.success()
    }
}
