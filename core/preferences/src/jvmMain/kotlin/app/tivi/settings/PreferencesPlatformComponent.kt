// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import app.tivi.inject.ApplicationScope
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences
import me.tatarka.inject.annotations.Provides

actual interface PreferencesPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideSettings(delegate: Preferences): ObservableSettings = PreferencesSettings(delegate)
}
