// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

expect interface LoggerPlatformComponent

interface LoggerComponent : LoggerPlatformComponent {
    @ApplicationScope
    @Provides
    fun bindRecordingLogger(): RecordingLogger = RecordingLoggerImpl()
}
