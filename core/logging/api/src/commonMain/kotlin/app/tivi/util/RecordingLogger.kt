// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import kotlinx.coroutines.flow.StateFlow

interface RecordingLogger : Logger {
    val buffer: StateFlow<List<LogMessage>>
}

enum class Severity {
    Verbose,
    Debug,
    Info,
    Warn,
    Error,
    Assert,
}

class LogMessage(
    val severity: Severity,
    message: () -> String,
    val throwable: Throwable?,
) {
    val message: String by lazy { message() }
}
