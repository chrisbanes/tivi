// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import me.tatarka.inject.annotations.Inject

@Inject
class TiviWorkerFactory(
    private val syncLibraryShows: (Context, WorkerParameters) -> SyncLibraryShows,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        name<SyncLibraryShows>() -> syncLibraryShows(appContext, workerParameters)
        else -> null
    }

    private inline fun <reified C> name() = C::class.qualifiedName
}
