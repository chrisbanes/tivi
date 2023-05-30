// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import java.io.File
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class DeleteFolder(
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<DeleteFolder.Params, Unit>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            if (params.directory.exists()) {
                params.directory.deleteRecursively()
            }
        }
    }

    data class Params(val directory: File)
}
