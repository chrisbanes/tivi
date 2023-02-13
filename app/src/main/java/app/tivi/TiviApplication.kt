/*
 * Copyright 2017 Google LLC
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

package app.tivi

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkerFactory
import app.tivi.extensions.unsafeLazy
import app.tivi.inject.ApplicationComponent
import app.tivi.inject.create

class TiviApplication : Application(), Configuration.Provider {
    val component: ApplicationComponent by unsafeLazy { ApplicationComponent::class.create(this) }

    private lateinit var workerFactory: WorkerFactory

    override fun onCreate() {
        super.onCreate()

        workerFactory = component.workerFactory

        component.initializers.init()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
