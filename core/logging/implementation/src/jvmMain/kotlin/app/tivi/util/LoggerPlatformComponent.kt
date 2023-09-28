// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
actual interface LoggerPlatformComponent {
    @Provides
    @ApplicationScope
    fun provideLogger(
        kermitLogger: KermitLogger,
        recordingLogger: RecordingLogger,
    ): Logger = CompositeLogger(kermitLogger, recordingLogger)
}
