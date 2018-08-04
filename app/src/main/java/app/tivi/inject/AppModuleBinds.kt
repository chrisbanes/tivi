/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.inject

import app.tivi.AppNavigator
import app.tivi.TiviAppNavigator
import app.tivi.actions.ShowTasks
import app.tivi.tasks.ShowTasksImpl
import app.tivi.util.Logger
import app.tivi.util.TimberLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
abstract class AppModuleBinds {
    @Singleton
    @Binds
    abstract fun provideTiviActions(bind: ShowTasksImpl): ShowTasks

    @Singleton
    @Named("app")
    @Binds
    abstract fun provideAppNavigator(bind: TiviAppNavigator): AppNavigator

    @Singleton
    @Binds
    abstract fun provideLogger(bind: TimberLogger): Logger
}