// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import app.tivi.inject.ApplicationScope
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSUserDefaults

actual interface PreferencesPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideSettings(delegate: NSUserDefaults): ObservableSettings = NSUserDefaultsSettings(delegate)
}
