// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface LoggerPlatformComponent {
    @ApplicationScope
    @Provides
    fun provideLogger(bind: TimberLogger): Logger = bind
}
