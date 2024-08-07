// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tivi.domain.interactors.UpdateLibraryShows
import co.touchlab.kermit.Logger
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class SyncLibraryShowsWorker(
  @Assisted context: Context,
  @Assisted params: WorkerParameters,
  private val updateLibraryShows: Lazy<UpdateLibraryShows>,
) : CoroutineWorker(context, params) {
  companion object {
    internal const val NAME = "night-sync-all-followed-shows"
  }

  override suspend fun doWork(): Result {
    Logger.d { "$NAME worker running" }
    val result = updateLibraryShows.value(UpdateLibraryShows.Params(true))
    return when {
      result.isSuccess -> Result.success()
      else -> Result.failure()
    }
  }
}
