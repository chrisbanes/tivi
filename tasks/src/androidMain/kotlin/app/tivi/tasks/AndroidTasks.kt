// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidTasks(
  workManager: Lazy<WorkManager>,
) : Tasks {
  private val workManager by workManager

  override fun registerPeriodicTasks() {
    val nightlyConstraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.UNMETERED)
      .setRequiresCharging(true)
      .build()

    workManager.enqueueUniquePeriodicWork(
      SyncLibraryShowsWorker.NAME,
      ExistingPeriodicWorkPolicy.UPDATE,
      PeriodicWorkRequestBuilder<SyncLibraryShowsWorker>(LIBRARY_SYNC_INTERVAL.toJavaDuration())
        .setConstraints(nightlyConstraints)
        .build(),
    )

    workManager.enqueueUniquePeriodicWork(
      ScheduleEpisodeNotificationsWorker.NAME,
      ExistingPeriodicWorkPolicy.UPDATE,
      PeriodicWorkRequestBuilder<ScheduleEpisodeNotificationsWorker>(
        SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL.toJavaDuration(),
      ).build(),
    )
  }

  override fun enqueueStartupTasks() {
    workManager.enqueueUniqueWork(
      "tasks_startup",
      ExistingWorkPolicy.KEEP,
      OneTimeWorkRequest.from(ScheduleEpisodeNotificationsWorker::class.java),
    )
  }

  internal companion object {
    val LIBRARY_SYNC_INTERVAL = 12.hours
    val SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL = 6.hours
  }
}
