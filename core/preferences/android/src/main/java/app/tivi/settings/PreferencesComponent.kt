/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
