// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import kotlinx.coroutines.flow.Flow

interface Preference<T> {
  val defaultValue: T

  val flow: Flow<T>
  suspend fun set(value: T)
  suspend fun get(): T
}

suspend fun Preference<Boolean>.toggle() = set(!get())
