// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidShowTasks(
  workManager: Lazy<WorkManager>,
) : ShowTasks {
  private val workManager by workManager

  override fun registerPeriodicTasks() {
    val nightlyConstraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.UNMETERED)
      .setRequiresCharging(true)
      .build()

    workManager.enqueueUniquePeriodicWork(
      SyncLibraryShowsWorker.TAG,
      ExistingPeriodicWorkPolicy.UPDATE,
      PeriodicWorkRequestBuilder<SyncLibraryShowsWorker>(
        LIBRARY_NIGHTLY_SYNC_INTERVAL.toJavaDuration(),
      )
        .setConstraints(nightlyConstraints)
        .build(),
    )

    workManager.enqueueUniquePeriodicWork(
      ScheduleEpisodeNotificationsWorker.TAG,
      ExistingPeriodicWorkPolicy.UPDATE,
      PeriodicWorkRequestBuilder<ScheduleEpisodeNotificationsWorker>(
        SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL.toJavaDuration(),
      ).build(),
    )
  }

  override fun enqueueStartupTasks() {
    workManager.enqueue(
      OneTimeWorkRequest.from(ScheduleEpisodeNotificationsWorker::class.java),
    )
  }

  internal companion object {
    val LIBRARY_NIGHTLY_SYNC_INTERVAL = 24.hours
    val SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL = 6.hours
  }
}
