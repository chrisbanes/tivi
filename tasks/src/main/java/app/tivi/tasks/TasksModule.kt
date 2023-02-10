/*
 * Copyright 2020 Google LLC
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

package app.tivi.tasks

import android.app.Application
import androidx.work.WorkManager
import app.tivi.actions.ShowTasks
import app.tivi.appinitializers.AppInitializer
import javax.inject.Singleton
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

@Component
abstract class TasksModule {
    @Provides
    @Singleton
    fun provideWorkManager(application: Application): WorkManager {
        return WorkManager.getInstance(application)
    }

    @Provides
    @IntoSet
    fun provideShowTasksInitializer(bind: ShowTasksInitializer): AppInitializer = bind

    @Provides
    @Singleton
    fun provideShowTasks(bind: ShowTasksImpl): ShowTasks = bind
}
