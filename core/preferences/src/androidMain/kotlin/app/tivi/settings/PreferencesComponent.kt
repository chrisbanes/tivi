// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package app.tivi.settings

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface PreferencesComponent {
    @ApplicationScope
    @Provides
    fun providePreferences(bind: TiviPreferencesImpl): TiviPreferences = bind

    @ApplicationScope
    @Provides
    fun provideAppPreferences(
        context: Application,
    ): AppSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
}

typealias AppSharedPreferences = SharedPreferences
