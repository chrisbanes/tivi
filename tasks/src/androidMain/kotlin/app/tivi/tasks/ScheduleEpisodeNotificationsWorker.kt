// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tivi.domain.interactors.ScheduleEpisodeNotifications
import co.touchlab.kermit.Logger
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ScheduleEpisodeNotificationsWorker(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  private val scheduleEpisodeNotifications: Lazy<ScheduleEpisodeNotifications>,
) : CoroutineWorker(context, params) {
  companion object {
    internal const val NAME = "set-episode-notifications"
  }

  override suspend fun doWork(): Result {
    Logger.d { "$NAME worker running" }
    val result = scheduleEpisodeNotifications.value.invoke(
      ScheduleEpisodeNotifications.Params(
        // We always schedule notifications for longer than the next task schedule, just in case
        // the task doesn't run on time
        AndroidTasks.SCHEDULE_EPISODE_NOTIFICATIONS_INTERVAL * 1.5,
      ),
    )
    return when {
      result.isSuccess -> Result.success()
      else -> Result.failure()
    }
  }
}
