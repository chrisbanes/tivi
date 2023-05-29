// Copyright 2020, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface TasksComponent : TasksPlatformComponent {
    @ApplicationScope
    @Provides
    @IntoSet
    fun provideShowTasksInitializer(bind: ShowTasksInitializer): AppInitializer = bind
}

expect interface TasksPlatformComponent
