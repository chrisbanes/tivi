/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.appinitializers

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import app.tivi.actions.ShowTasks
import app.tivi.tasks.inject.TiviWorkerFactory
import javax.inject.Inject
import javax.inject.Provider

class ShowTasksInitializer @Inject constructor(
    private val showTasks: Provider<ShowTasks>,
    private val workerFactory: TiviWorkerFactory
) : AppInitializer {
    override fun init(application: Application) {
        val wmConfig = Configuration.Builder().setWorkerFactory(workerFactory).build()
        WorkManager.initialize(application, wmConfig)

        // This needs to be a Provider so that the initialize above is called
        // before WorkManager.getInstance()
        showTasks.get().setupNightSyncs()
    }
}