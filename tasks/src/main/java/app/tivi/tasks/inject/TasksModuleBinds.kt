/*
 * Copyright 2019 Google LLC
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

package app.tivi.tasks.inject

import app.tivi.actions.ShowTasks
import app.tivi.appinitializers.AppInitializer
import app.tivi.appinitializers.ShowTasksInitializer
import app.tivi.tasks.ShowTasksImpl
import app.tivi.tasks.SyncAllFollowedShows
import app.tivi.tasks.SyncShowWatchedProgress
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
abstract class TasksModuleBinds {
    @Binds
    @IntoMap
    @WorkerKey(SyncAllFollowedShows::class)
    abstract fun bindSyncAllFollowedShows(factory: SyncAllFollowedShows.Factory): ChildWorkerFactory

    @Binds
    @IntoMap
    @WorkerKey(SyncShowWatchedProgress::class)
    abstract fun bindSyncShowWatchedProgress(factory: SyncShowWatchedProgress.Factory): ChildWorkerFactory

    @Binds
    @IntoSet
    abstract fun provideShowTasksInitializer(bind: ShowTasksInitializer): AppInitializer

    @Binds
    @Singleton
    abstract fun provideTiviActions(bind: ShowTasksImpl): ShowTasks
}