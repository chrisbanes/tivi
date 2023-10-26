// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package app.tivi.settings

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import app.tivi.inject.ApplicationScope
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import me.tatarka.inject.annotations.Provides

actual interface PreferencesPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideSettings(delegate: AppSharedPreferences): ObservableSettings {
    return SharedPreferencesSettings(delegate)
  }

  @ApplicationScope
  @Provides
  fun provideAppPreferences(
    context: Application,
  ): AppSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}

typealias AppSharedPreferences = SharedPreferences
