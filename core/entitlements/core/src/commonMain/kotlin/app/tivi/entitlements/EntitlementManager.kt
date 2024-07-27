// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface EntitlementManager {
  fun setup() = Unit
  suspend fun hasProEntitlement(): Boolean
  fun observeProEntitlement(): Flow<Boolean>

  companion object {
    val Always: EntitlementManager = object : EntitlementManager {
      override suspend fun hasProEntitlement(): Boolean = true
      override fun observeProEntitlement(): Flow<Boolean> = flowOf(true)
    }
  }
}
