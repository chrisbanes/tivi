// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import app.tivi.settings.TiviPreferences.Theme
import app.tivi.util.AppCoroutineDispatchers
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
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

    override var useDynamicColors: Boolean by BooleanDelegate(KEY_USE_DYNAMIC_COLORS, true)

    override fun observeUseDynamicColors(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_USE_DYNAMIC_COLORS, true)
    }

    override var useLessData: Boolean by BooleanDelegate(KEY_DATA_SAVER, false)

    override fun observeUseLessData(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_DATA_SAVER, false)
    }

    override var libraryFollowedActive: Boolean by BooleanDelegate(KEY_LIBRARY_FOLLOWED_ACTIVE, true)

    override fun observeLibraryFollowedActive(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_LIBRARY_FOLLOWED_ACTIVE, true)
    }

    override var libraryWatchedActive: Boolean by BooleanDelegate(KEY_LIBRARY_WATCHED_ACTIVE, true)

    override fun observeLibraryWatchedActive(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_LIBRARY_WATCHED_ACTIVE, true)
    }

    override var upNextFollowedOnly: Boolean by BooleanDelegate(KEY_UPNEXT_FOLLOWED_ONLY, false)

    override fun observeUpNextFollowedOnly(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_UPNEXT_FOLLOWED_ONLY, false)
    }

    override var ignoreSpecials: Boolean by BooleanDelegate(KEY_IGNORE_SPECIALS, true)

    override fun observeIgnoreSpecials(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_IGNORE_SPECIALS, true)
    }

    override var developerHideArtwork: Boolean by BooleanDelegate(KEY_DEV_HIDE_ARTWORK, false)

    override fun observeDeveloperHideArtwork(): Flow<Boolean> {
        return flowSettings.getBooleanFlow(KEY_DEV_HIDE_ARTWORK, false)
    }

    private class BooleanDelegate(
        private val key: String,
        private val defaultValue: Boolean,
    ) : ReadWriteProperty<TiviPreferencesImpl, Boolean> {
        override fun getValue(thisRef: TiviPreferencesImpl, property: KProperty<*>): Boolean {
            return thisRef.settings.getBoolean(key, defaultValue)
        }

        override fun setValue(thisRef: TiviPreferencesImpl, property: KProperty<*>, value: Boolean) {
            thisRef.settings.putBoolean(key, value)
        }
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
internal const val KEY_IGNORE_SPECIALS = "pref_ignore_specials"

internal const val KEY_DEV_HIDE_ARTWORK = "pref_dev_hide_artwork"

internal const val THEME_LIGHT_VALUE = "light"
internal const val THEME_DARK_VALUE = "dark"
internal const val THEME_SYSTEM_VALUE = "system"
