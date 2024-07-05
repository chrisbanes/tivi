// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tivi.domain.interactors.ScheduleEpisodeNotifications
import app.tivi.util.Logger
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ScheduleEpisodeNotificationsWorker(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  private val scheduleEpisodeNotifications: Lazy<ScheduleEpisodeNotifications>,
  private val logger: Logger,
) : CoroutineWorker(context, params) {
  companion object {
    internal const val TAG = "set-episode-notifications"
  }

  override suspend fun doWork(): Result {
    logger.d { "$TAG worker running" }
    val result = scheduleEpisodeNotifications.value.invoke(Unit)
    return when {
      result.isSuccess -> Result.success()
      else -> Result.failure()
    }
  }
}
