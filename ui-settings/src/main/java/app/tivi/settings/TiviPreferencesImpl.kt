/*
 * Copyright 2018 Google LLC
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

package app.tivi.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import app.tivi.settings.TiviPreferences.Theme
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

class TiviPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("app") private val sharedPreferences: SharedPreferences
) : TiviPreferences {
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_THEME -> updateUsingThemePreference()
        }
    }

    private val defaultThemeValue = context.getString(R.string.pref_theme_default_value)

    companion object {
        const val KEY_THEME = "pref_theme"
        const val KEY_DATA_SAVER = "pref_data_saver"
    }

    override fun setup() {
        updateUsingThemePreference()
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override var themePreference: Theme
        get() = getThemeForStorageValue(sharedPreferences.getString(KEY_THEME, defaultThemeValue)!!)
        set(value) = sharedPreferences.edit {
            putString(KEY_THEME, value.storageKey)
        }

    override var useLessData: Boolean
        get() = sharedPreferences.getBoolean(KEY_DATA_SAVER, false)
        set(value) = sharedPreferences.edit {
            putBoolean(KEY_DATA_SAVER, value)
        }

    val Theme.storageKey: String
        get() = when (this) {
            Theme.LIGHT -> context.getString(R.string.pref_theme_light_value)
            Theme.DARK -> context.getString(R.string.pref_theme_dark_value)
            Theme.SYSTEM -> context.getString(R.string.pref_theme_system_value)
        }

    private fun getThemeForStorageValue(value: String) = when (value) {
        context.getString(R.string.pref_theme_light_value) -> Theme.LIGHT
        context.getString(R.string.pref_theme_dark_value) -> Theme.DARK
        else -> Theme.SYSTEM
    }

    private fun updateUsingThemePreference() = when (themePreference) {
        Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Theme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
