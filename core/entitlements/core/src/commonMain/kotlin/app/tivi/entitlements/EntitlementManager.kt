// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements

interface EntitlementManager {

  fun setup()

  suspend fun hasProEntitlement(): Boolean
}
