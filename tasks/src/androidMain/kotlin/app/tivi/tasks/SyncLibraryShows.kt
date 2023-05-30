// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.tivi.domain.interactors.UpdateLibraryShows
import app.tivi.util.Logger
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class SyncLibraryShows(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val updateLibraryShows: UpdateLibraryShows,
    private val logger: Logger,
) : CoroutineWorker(context, params) {
    companion object {
        internal const val TAG = "sync-all-followed-shows"
        internal const val NIGHTLY_SYNC_TAG = "night-sync-all-followed-shows"
    }

    override suspend fun doWork(): Result {
        logger.d("$tags worker running")
        val result = updateLibraryShows(UpdateLibraryShows.Params(true))
        return when {
            result.isSuccess -> Result.success()
            else -> Result.failure()
        }
    }
}
