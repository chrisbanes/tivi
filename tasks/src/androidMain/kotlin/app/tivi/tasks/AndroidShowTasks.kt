// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.tivi.extensions.fluentIf
import java.util.concurrent.TimeUnit
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidShowTasks(
    private val workManager: WorkManager,
) : ShowTasks {
    override fun syncLibraryShows(deferUntilIdle: Boolean) {
        val request = OneTimeWorkRequestBuilder<SyncLibraryShows>()
            .addTag(SyncLibraryShows.TAG)
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
            SyncLibraryShows.NIGHTLY_SYNC_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SyncLibraryShows>(24, TimeUnit.HOURS)
                .setConstraints(nightlyConstraints)
                .build(),
        )
    }
}
