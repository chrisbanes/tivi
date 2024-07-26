// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements

import app.tivi.appinitializers.AppInitializer
import me.tatarka.inject.annotations.Inject

@Inject
class EntitlementManagerInitializer(
  private val entitlementManager: Lazy<EntitlementManager>,
) : AppInitializer {
  override fun initialize() {
    entitlementManager.value.setup()
  }
}
