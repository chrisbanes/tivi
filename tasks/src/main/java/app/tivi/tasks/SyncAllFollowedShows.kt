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
import androidx.work.WorkerParameters
import app.tivi.data.entities.RefreshType
import app.tivi.domain.interactors.UpdateFollowedShows
import app.tivi.util.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncAllFollowedShows @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val updateFollowedShows: UpdateFollowedShows,
    private val logger: Logger
) : CoroutineWorker(context, params) {
    companion object {
        const val TAG = "sync-all-followed-shows"
        const val NIGHTLY_SYNC_TAG = "night-sync-all-followed-shows"
    }

    override suspend fun doWork(): Result {
        logger.d("$TAG worker running")
        updateFollowedShows.executeSync(UpdateFollowedShows.Params(true, RefreshType.FULL))
        return Result.success()
    }
}
