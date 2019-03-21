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

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TiviPreferences @Inject constructor(
    @Named("app") private val sharedPreferences: SharedPreferences
) {
    companion object {
        const val KEY_THEME = "pref_theme"
    }

    enum class Theme(val value: String) {
        LIGHT("light"),
        DARK("dark"),
        BATTERY_SAVER_ONLY("battery"),
        SYSTEM("system")
    }

    val themePreference: Theme
        get() = uiThemeFromPref(sharedPreferences.getString(KEY_THEME, null))

    private fun uiThemeFromPref(value: String?): Theme {
        return Theme.values().firstOrNull { it.value == value } ?: Theme.SYSTEM
    }
}
