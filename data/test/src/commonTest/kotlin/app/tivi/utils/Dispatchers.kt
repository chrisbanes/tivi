// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.utils

import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher

fun createSingleAppCoroutineDispatchers(
  testDispatcher: CoroutineDispatcher = StandardTestDispatcher(),
): AppCoroutineDispatchers = AppCoroutineDispatchers(
  io = testDispatcher,
  databaseRead = testDispatcher,
  databaseWrite = testDispatcher,
  computation = testDispatcher,
  main = testDispatcher,
)
