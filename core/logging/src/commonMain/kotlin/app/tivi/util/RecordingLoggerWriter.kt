// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private const val BUFFER_SIZE: Int = 100

object RecordingLoggerWriter : LogWriter() {
  private val _buffer = MutableStateFlow<List<LogMessage>>(ArrayList(BUFFER_SIZE))

  val buffer: Flow<List<LogMessage>> = _buffer.asStateFlow()

  override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
    addLog(LogMessage(severity, message, throwable))
  }

  private fun addLog(logMessage: LogMessage) {
    _buffer.update { logs ->
      logs.toMutableList().apply {
        while (logs.size > BUFFER_SIZE - 1) {
          removeFirst()
        }
        add(logMessage)
      }
    }
  }
}

data class LogMessage(
  val severity: Severity,
  val message: String,
  val throwable: Throwable?,
  val timestamp: Instant = Clock.System.now(),
)
