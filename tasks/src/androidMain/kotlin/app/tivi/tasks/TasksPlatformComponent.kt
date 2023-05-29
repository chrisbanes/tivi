// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import android.app.Application
import androidx.work.WorkManager
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface TasksPlatformComponent {
    @ApplicationScope
    @Provides
    fun provideShowTasks(bind: AndroidShowTasks): ShowTasks = bind

    @ApplicationScope
    @Provides
    fun provideWorkManager(application: Application): WorkManager {
        return WorkManager.getInstance(application)
    }
}
