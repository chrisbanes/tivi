// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import kotlinx.coroutines.CoroutineDispatcher

data class AppCoroutineDispatchers(
  val io: CoroutineDispatcher,
  val databaseWrite: CoroutineDispatcher,
  val databaseRead: CoroutineDispatcher,
  val computation: CoroutineDispatcher,
  val main: CoroutineDispatcher,
)
