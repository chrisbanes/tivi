// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class UiMessage(
  val message: String,
  val id: Long = uuid4().mostSignificantBits,
)

fun UiMessage(
  t: Throwable,
  id: Long = uuid4().mostSignificantBits,
): UiMessage = UiMessage(
  message = t.message ?: "Error occurred: $t",
  id = id,
)

class UiMessageManager {
  private val mutex = Mutex()

  private val _message = MutableStateFlow(emptyList<UiMessage>())

  /**
   * A flow emitting the current message to display.
   */
  val message: Flow<UiMessage?> = _message.map { it.firstOrNull() }.distinctUntilChanged()

  suspend fun emitMessage(message: UiMessage) {
    mutex.withLock {
      _message.value = _message.value + message
    }
  }

  suspend fun clearMessage(id: Long) {
    mutex.withLock {
      _message.value = _message.value.filterNot { it.id == id }
    }
  }
}
