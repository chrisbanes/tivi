// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

data class DatabaseConfiguration(
  val name: String = "tivi",
  val inMemory: Boolean = false,
)
