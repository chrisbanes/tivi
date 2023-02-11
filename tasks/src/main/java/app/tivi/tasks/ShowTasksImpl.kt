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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.tivi.actions.ShowTasks
import app.tivi.extensions.fluentIf
import java.util.concurrent.TimeUnit
import me.tatarka.inject.annotations.Inject

@Inject
class ShowTasksImpl(
    private val workManager: WorkManager,
) : ShowTasks {
    override fun syncFollowedShows(deferUntilIdle: Boolean) {
        val request = OneTimeWorkRequestBuilder<SyncAllFollowedShows>()
            .addTag(SyncAllFollowedShows.TAG)
            .fluentIf(deferUntilIdle) {
                setConstraints(
                    Constraints.Builder()
                        .setRequiresDeviceIdle(true)
                        .build(),
                )
            }
            .build()
        workManager.enqueue(request)
    }

    override fun syncWatchedShows(deferUntilIdle: Boolean) {
        val request = OneTimeWorkRequestBuilder<SyncWatchedShows>()
            .addTag(SyncWatchedShows.TAG)
            .fluentIf(deferUntilIdle) {
                setConstraints(
                    Constraints.Builder()
                        .setRequiresDeviceIdle(true)
                        .build(),
                )
            }
            .build()
        workManager.enqueue(request)
    }

    override fun setupNightSyncs() {
        val nightlyConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncAllFollowedShows.NIGHTLY_SYNC_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SyncAllFollowedShows>(24, TimeUnit.HOURS)
                .setConstraints(nightlyConstraints)
                .build(),
        )

        workManager.enqueueUniquePeriodicWork(
            SyncWatchedShows.NIGHTLY_SYNC_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SyncWatchedShows>(24, TimeUnit.HOURS)
                .setConstraints(nightlyConstraints)
                .build(),
        )
    }
}
