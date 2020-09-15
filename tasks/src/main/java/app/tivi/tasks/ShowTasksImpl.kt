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

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import app.tivi.actions.ShowTasks
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ShowTasksImpl @Inject constructor(
    private val workManager: WorkManager
) : ShowTasks {
    override fun syncShowWatchedEpisodes(showId: Long) {
        val request = OneTimeWorkRequest.Builder(SyncShowWatchedProgress::class.java)
            .addTag(SyncShowWatchedProgress.TAG)
            .setInputData(SyncShowWatchedProgress.buildData(showId))
            .build()
        workManager.enqueue(request)
    }

    override fun syncFollowedShows() {
        val request = OneTimeWorkRequest.Builder(SyncAllFollowedShows::class.java)
            .addTag(SyncAllFollowedShows.TAG)
            .build()
        workManager.enqueue(request)
    }

    override fun syncFollowedShowsWhenIdle() {
        val request = OneTimeWorkRequest.Builder(SyncAllFollowedShows::class.java)
            .addTag(SyncAllFollowedShows.TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresDeviceIdle(true)
                    .build()
            )
            .build()
        workManager.enqueue(request)
    }

    override fun setupNightSyncs() {
        val request = PeriodicWorkRequest.Builder(
            SyncAllFollowedShows::class.java,
            24L,
            TimeUnit.HOURS,
            12L,
            TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()
        ).build()

        workManager.enqueueUniquePeriodicWork(
            SyncAllFollowedShows.NIGHTLY_SYNC_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
}
