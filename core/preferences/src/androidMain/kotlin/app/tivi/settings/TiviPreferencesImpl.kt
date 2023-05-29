// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import app.tivi.core.preferences.R
import app.tivi.settings.TiviPreferences.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.tatarka.inject.annotations.Inject

@Inject
class TiviPreferencesImpl(
    private val context: Application,
    private val sharedPreferences: AppSharedPreferences,
) : TiviPreferences {
    private val defaultThemeValue = context.getString(R.string.pref_theme_default_value)

    private val defaultUseDynamicColors = Build.VERSION.SDK_INT >= 31

    private val preferenceKeyChangedFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        preferenceKeyChangedFlow.tryEmit(key)
    }

    companion object {
        const val KEY_THEME = "pref_theme"
        const val KEY_USE_DYNAMIC_COLORS = "pref_dynamic_colors"
        const val KEY_DATA_SAVER = "pref_data_saver"
        const val KEY_LIBRARY_FOLLOWED_ACTIVE = "pref_library_followed_active"
        const val KEY_LIBRARY_WATCHED_ACTIVE = "pref_library_watched_active"
        const val KEY_UPNEXT_FOLLOWED_ONLY = "pref_upnext_followedonly_active"
    }

    override fun setup() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override var theme: Theme
        get() = getThemeForStorageValue(sharedPreferences.getString(KEY_THEME, defaultThemeValue)!!)
        set(value) = sharedPreferences.edit {
            putString(KEY_THEME, value.storageKey)
        }

    override fun observeTheme(): Flow<Theme> = createPreferenceFlow(KEY_THEME) { theme }

    override var useDynamicColors: Boolean
        get() = sharedPreferences.getBoolean(KEY_USE_DYNAMIC_COLORS, defaultUseDynamicColors)
        set(value) = sharedPreferences.edit {
            putBoolean(KEY_USE_DYNAMIC_COLORS, value)
        }

    override fun observeUseDynamicColors(): Flow<Boolean> {
        return createPreferenceFlow(KEY_USE_DYNAMIC_COLORS) { useDynamicColors }
    }

    override var useLessData: Boolean
        get() = sharedPreferences.getBoolean(KEY_DATA_SAVER, false)
        set(value) = sharedPreferences.edit {
            putBoolean(KEY_DATA_SAVER, value)
        }

    override fun observeUseLessData(): Flow<Boolean> = createPreferenceFlow(KEY_DATA_SAVER) {
        useLessData
    }

    override var libraryFollowedActive: Boolean
        get() = sharedPreferences.getBoolean(KEY_LIBRARY_FOLLOWED_ACTIVE, true)
        set(value) = sharedPreferences.edit {
            putBoolean(KEY_LIBRARY_FOLLOWED_ACTIVE, value)
        }

    override fun observeLibraryFollowedActive(): Flow<Boolean> {
        return createPreferenceFlow(KEY_LIBRARY_FOLLOWED_ACTIVE) {
            libraryFollowedActive
        }
    }

    override var libraryWatchedActive: Boolean
        get() = sharedPreferences.getBoolean(KEY_LIBRARY_WATCHED_ACTIVE, true)
        set(value) = sharedPreferences.edit {
            putBoolean(KEY_LIBRARY_WATCHED_ACTIVE, value)
        }

    override fun observeLibraryWatchedActive(): Flow<Boolean> {
        return createPreferenceFlow(KEY_LIBRARY_WATCHED_ACTIVE) {
            libraryWatchedActive
        }
    }

    override var upNextFollowedOnly: Boolean
        get() = sharedPreferences.getBoolean(KEY_UPNEXT_FOLLOWED_ONLY, false)
        set(value) = sharedPreferences.edit {
            putBoolean(KEY_UPNEXT_FOLLOWED_ONLY, value)
        }

    override fun observeUpNextFollowedOnly(): Flow<Boolean> {
        return createPreferenceFlow(KEY_UPNEXT_FOLLOWED_ONLY) { upNextFollowedOnly }
    }

    private inline fun <T> createPreferenceFlow(
        key: String,
        crossinline getValue: () -> T,
    ): Flow<T> = preferenceKeyChangedFlow
        // Emit on start so that we always send the initial value
        .onStart { emit(key) }
        .filter { it == key }
        .map { getValue() }
        .distinctUntilChanged()

    private val Theme.storageKey: String
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
}
