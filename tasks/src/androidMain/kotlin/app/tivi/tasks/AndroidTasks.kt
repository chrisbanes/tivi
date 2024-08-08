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
import co.touchlab.kermit.Logger
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidTasks(
  workManager: Lazy<WorkManager>,
) : Tasks {
  private val workManager by workManager
  private val logger by lazy { Logger.withTag("AndroidTasks") }

  override fun scheduleLibrarySync() {
    logger.d { "Tasks.scheduleLibrarySync()" }

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
  }

  override fun cancelLibrarySync() {
    logger.d { "Tasks.cancelLibrarySync()" }
    workManager.cancelUniqueWork(SyncLibraryShowsWorker.NAME)
  }

  override fun scheduleEpisodeNotifications() {
    logger.d { "Tasks.scheduleEpisodeNotifications()" }

    workManager.enqueueUniqueWork(
      "tasks_startup",
      ExistingWorkPolicy.KEEP,
      OneTimeWorkRequest.from(ScheduleEpisodeNotificationsWorker::class.java),
    )

    workManager.enqueueUniquePeriodicWork(
      ScheduleEpisodeNotificationsWorker.NAME,
      ExistingPeriodicWorkPolicy.UPDATE,
      PeriodicWorkRequestBuilder<ScheduleEpisodeNotificationsWorker>(
        SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL.toJavaDuration(),
      ).build(),
    )
  }

  override fun cancelEpisodeNotifications() {
    logger.d { "Tasks.cancelEpisodeNotifications()" }
    workManager.cancelUniqueWork(ScheduleEpisodeNotificationsWorker.NAME)
  }

  internal companion object {
    val LIBRARY_SYNC_INTERVAL = 12.hours
    val SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL = 6.hours
  }
}
