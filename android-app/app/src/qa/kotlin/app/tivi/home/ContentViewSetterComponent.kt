// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import app.tivi.ContentViewSetter
import au.com.gridstone.debugdrawer.DebugDrawer
import au.com.gridstone.debugdrawer.DeviceInfoModule
import au.com.gridstone.debugdrawer.okhttplogs.HttpLogger
import au.com.gridstone.debugdrawer.okhttplogs.OkHttpLoggerModule
import au.com.gridstone.debugdrawer.timber.TimberModule
import me.tatarka.inject.annotations.Provides

interface ContentViewSetterComponent {
    @Provides
    fun provideContentViewSetter(
        httpLogger: HttpLogger,
    ): ContentViewSetter = ContentViewSetter { activity, view ->
        DebugDrawer.with(activity)
            .addSectionTitle("Logs")
            .addModule(OkHttpLoggerModule(httpLogger))
            .addModule(TimberModule())
            .addSectionTitle("Device information")
            .addModule(DeviceInfoModule())
            .buildMainContainer()
            .apply { addView(view) }
    }
}
