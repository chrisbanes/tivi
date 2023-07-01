// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import app.tivi.appinitializers.AppInitializer
import me.tatarka.inject.annotations.Inject

@Inject
class PreferencesInitializer(
    private val prefs: TiviPreferences,
) : AppInitializer {
    override fun initialize() {
        prefs.setup()
    }
}
