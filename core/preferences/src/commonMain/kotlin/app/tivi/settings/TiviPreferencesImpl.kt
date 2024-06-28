// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import app.tivi.settings.TiviPreferences.Theme
import app.tivi.util.AppCoroutineDispatchers
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalSettingsApi::class)
@Inject
class TiviPreferencesImpl(
  settings: Lazy<ObservableSettings>,
  private val dispatchers: AppCoroutineDispatchers,
) : TiviPreferences {
  private val settings: ObservableSettings by settings
  private val flowSettings by lazy { settings.value.toFlowSettings(dispatchers.io) }

  override suspend fun setTheme(theme: Theme) = withContext(dispatchers.io) {
    settings.putString(KEY_THEME, theme.storageKey)
  }

  override fun observeTheme(): Flow<Theme> {
    return settings.getStringFlow(KEY_THEME, THEME_SYSTEM_VALUE)
      .map(::getThemeForStorageValue)
  }

  override suspend fun toggleUseDynamicColors() = withContext(dispatchers.io) {
    settings.toggleBoolean(KEY_USE_DYNAMIC_COLORS, true)
  }

  override fun observeUseDynamicColors(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_USE_DYNAMIC_COLORS, true)
  }

  override suspend fun getUseLessData(): Boolean = withContext(dispatchers.io) {
    settings[KEY_DATA_SAVER, false]
  }

  override suspend fun toggleUseLessData() = withContext(dispatchers.io) {
    settings.toggleBoolean(KEY_DATA_SAVER)
  }

  override fun observeUseLessData(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_DATA_SAVER, false)
  }

  override suspend fun toggleLibraryFollowedActive() {
    withContext(dispatchers.io) {
      settings.toggleBoolean(KEY_LIBRARY_FOLLOWED_ACTIVE, true)
    }
  }

  override fun observeLibraryFollowedActive(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_LIBRARY_FOLLOWED_ACTIVE, true)
  }

  override suspend fun toggleLibraryWatchedActive() {
    withContext(dispatchers.io) {
      settings.toggleBoolean(KEY_LIBRARY_WATCHED_ACTIVE, true)
    }
  }

  override fun observeLibraryWatchedActive(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_LIBRARY_WATCHED_ACTIVE, true)
  }

  override suspend fun toggleUpNextFollowedOnly() {
    withContext(dispatchers.io) {
      settings.toggleBoolean(KEY_UPNEXT_FOLLOWED_ONLY, false)
    }
  }

  override fun observeUpNextFollowedOnly(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_UPNEXT_FOLLOWED_ONLY, false)
  }

  override suspend fun toggleIgnoreSpecials() {
    withContext(dispatchers.io) {
      settings.toggleBoolean(KEY_IGNORE_SPECIALS, true)
    }
  }

  override fun observeIgnoreSpecials(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_IGNORE_SPECIALS, true)
  }

  override suspend fun toggleReportAppCrashes() {
    withContext(dispatchers.io) {
      settings.toggleBoolean(KEY_OPT_IN_CRASH_REPORTING, true)
    }
  }

  override fun observeReportAppCrashes(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_OPT_IN_CRASH_REPORTING, true)
  }

  override suspend fun toggleReportAnalytics() {
    withContext(dispatchers.io) {
      settings.toggleBoolean(KEY_OPT_IN_ANALYTICS_REPORTING, true)
    }
  }

  override fun observeReportAnalytics(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_OPT_IN_ANALYTICS_REPORTING, true)
  }

  override suspend fun toggleDeveloperHideArtwork() {
    withContext(dispatchers.io) {
      settings.toggleBoolean(KEY_DEV_HIDE_ARTWORK)
    }
  }

  override suspend fun getDeveloperHideArtwork(): Boolean = withContext(dispatchers.io) {
    settings.getBoolean(KEY_DEV_HIDE_ARTWORK, false)
  }

  override fun observeDeveloperHideArtwork(): Flow<Boolean> {
    return flowSettings.getBooleanFlow(KEY_DEV_HIDE_ARTWORK, false)
  }

  override val notificationsEnabled: Preference<Boolean> by lazy {
    BooleanPreference(KEY_NOTIFICATIONS, false)
  }

  private inner class BooleanPreference(
    private val key: String,
    private val defaultValue: Boolean = false,
  ) : Preference<Boolean> {
    override suspend fun set(value: Boolean) = withContext(dispatchers.io) {
      settings[key] = value
    }

    override suspend fun get(): Boolean = withContext(dispatchers.io) {
      settings.getBoolean(key, defaultValue)
    }

    override val flow: Flow<Boolean> by lazy { flowSettings.getBooleanFlow(key, defaultValue) }
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

internal const val KEY_NOTIFICATIONS = "pref_notifications"

internal const val KEY_OPT_IN_CRASH_REPORTING = "pref_opt_in_crash_reporting"
internal const val KEY_OPT_IN_ANALYTICS_REPORTING = "pref_opt_in_analytics_reporting"

internal const val KEY_DEV_HIDE_ARTWORK = "pref_dev_hide_artwork"

internal const val THEME_LIGHT_VALUE = "light"
internal const val THEME_DARK_VALUE = "dark"
internal const val THEME_SYSTEM_VALUE = "system"

private fun ObservableSettings.toggleBoolean(key: String, defaultValue: Boolean = false) {
  putBoolean(key, !getBoolean(key, defaultValue))
}
