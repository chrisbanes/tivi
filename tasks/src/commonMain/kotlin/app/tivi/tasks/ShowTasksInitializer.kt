// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.appinitializers.AppInitializer
import me.tatarka.inject.annotations.Inject

@Inject
class ShowTasksInitializer(
    private val showTasks: Lazy<ShowTasks>,
) : AppInitializer {
    override fun init() {
        showTasks.value.setupNightSyncs()
    }
}
