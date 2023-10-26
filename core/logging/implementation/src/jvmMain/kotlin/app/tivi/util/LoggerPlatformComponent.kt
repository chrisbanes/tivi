// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface LoggerPlatformComponent {
  @Provides
  @ApplicationScope
  fun provideLogger(
    kermitLogger: KermitLogger,
    recordingLogger: RecordingLogger,
  ): Logger = CompositeLogger(kermitLogger, recordingLogger)

  @Provides
  fun bindSetCrashReportingEnabledAction(): SetCrashReportingEnabledAction {
    return NoopSetCrashReportingEnabledAction
  }
}

private object NoopSetCrashReportingEnabledAction : SetCrashReportingEnabledAction {
  override fun invoke(enabled: Boolean) {}
}
