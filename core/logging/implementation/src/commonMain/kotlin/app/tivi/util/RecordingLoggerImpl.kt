// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class RecordingLoggerImpl(
    private val bufferSize: Int = 50,
) : RecordingLogger {

    private val logs = ArrayDeque<LogMessage>(bufferSize)
    private val _buffer = MutableStateFlow<List<LogMessage>>(logs)

    override val buffer get() = _buffer.asStateFlow()

    override fun v(throwable: Throwable?, message: () -> String) {
        addLog(LogMessage(Severity.Verbose, message, throwable))
    }

    override fun d(throwable: Throwable?, message: () -> String) {
        addLog(LogMessage(Severity.Debug, message, throwable))
    }

    override fun i(throwable: Throwable?, message: () -> String) {
        addLog(LogMessage(Severity.Info, message, throwable))
    }

    override fun e(throwable: Throwable?, message: () -> String) {
        addLog(LogMessage(Severity.Error, message, throwable))
    }

    override fun w(throwable: Throwable?, message: () -> String) {
        addLog(LogMessage(Severity.Warn, message, throwable))
    }

    private fun addLog(logMessage: LogMessage) {
        while (logs.size >= bufferSize) {
            logs.removeFirst()
        }
        logs.add(logMessage)
        _buffer.value = logs.toList()
    }
}
