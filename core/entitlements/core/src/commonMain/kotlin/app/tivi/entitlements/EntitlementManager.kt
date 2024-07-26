// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface EntitlementManager {
  fun setup() = Unit
  suspend fun hasProEntitlement(): Boolean = false
  fun observeProEntitlement(): Flow<Boolean> = emptyFlow()
}
