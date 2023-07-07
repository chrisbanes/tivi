// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import app.tivi.settings.TiviPreferences.Theme
import app.tivi.util.AppCoroutineDispatchers
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalSettingsApi::class)
@Inject
class TiviPreferencesImpl(
    private val settings: ObservableSettings,
    dispatchers: AppCoroutineDispatchers,
) : TiviPreferences {
    private val flowSettings by lazy { settings.toFlowSettings(dispatchers.io) }

    override var theme: Theme
        get() = getThemeForStorageValue(settings.getString(KEY_THEME, THEME_SYSTEM_VALUE))
        set(value) = settings.putString(KEY_THEME, value.storageKey)

    override fun observeTheme(): Flow<Theme> {
        return settings.getStringFlow(KEY_THEME, THEME_SYSTEM_VALUE)
            .map(::getThemeForStorageValue)
    }

    override var useDynamicColors: Boolean
        get() = settings.getBoolean(KEY_USE_DYNAMIC_COLORS, true)
        set(value) = settings.putBoolean(KEY_USE_DYNAMIC_COLORS, value)

    override fun observeUseDynamicColors(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_USE_DYNAMIC_COLORS, true)
    }

    override var useLessData: Boolean
        get() = settings.getBoolean(KEY_DATA_SAVER, false)
        set(value) = settings.putBoolean(KEY_DATA_SAVER, value)

    override fun observeUseLessData(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_DATA_SAVER, false)
    }

    override var libraryFollowedActive: Boolean
        get() = settings.getBoolean(KEY_LIBRARY_FOLLOWED_ACTIVE, true)
        set(value) = settings.putBoolean(KEY_LIBRARY_FOLLOWED_ACTIVE, value)

    override fun observeLibraryFollowedActive(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_LIBRARY_FOLLOWED_ACTIVE, true)
    }

    override var libraryWatchedActive: Boolean
        get() = settings.getBoolean(KEY_LIBRARY_WATCHED_ACTIVE, true)
        set(value) = settings.putBoolean(KEY_LIBRARY_WATCHED_ACTIVE, value)

    override fun observeLibraryWatchedActive(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_LIBRARY_WATCHED_ACTIVE, true)
    }

    override var upNextFollowedOnly: Boolean
        get() = settings.getBoolean(KEY_UPNEXT_FOLLOWED_ONLY, false)
        set(value) = settings.putBoolean(KEY_UPNEXT_FOLLOWED_ONLY, value)

    override fun observeUpNextFollowedOnly(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_UPNEXT_FOLLOWED_ONLY, false)
    }
}

private val Theme.storageKey: String
    get() = when (this) {
        Theme.LIGHT -> THEME_LIGHT_VALUE
        Theme.DARK -> THEME_DARK_VALUE
        Theme.SYSTEM -> THEME_SYSTEM_VALUE
    }

private fun getThemeForStorageValue(value: String) = when (value) {
    THEME_LIGHT_VALUE -> Theme.LIGHT
    THEME_DARK_VALUE -> Theme.DARK
    else -> Theme.SYSTEM
}

internal const val KEY_THEME = "pref_theme"
internal const val KEY_USE_DYNAMIC_COLORS = "pref_dynamic_colors"
internal const val KEY_DATA_SAVER = "pref_data_saver"
internal const val KEY_LIBRARY_FOLLOWED_ACTIVE = "pref_library_followed_active"
internal const val KEY_LIBRARY_WATCHED_ACTIVE = "pref_library_watched_active"
internal const val KEY_UPNEXT_FOLLOWED_ONLY = "pref_upnext_followedonly_active"

internal const val THEME_LIGHT_VALUE = "light"
internal const val THEME_DARK_VALUE = "dark"
internal const val THEME_SYSTEM_VALUE = "system"
