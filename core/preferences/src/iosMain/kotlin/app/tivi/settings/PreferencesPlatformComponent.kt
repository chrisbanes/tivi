// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface PreferencesPlatformComponent {
    @ApplicationScope
    @Provides
    fun providePreferences(): TiviPreferences = EmptyTiviPreferences
}
