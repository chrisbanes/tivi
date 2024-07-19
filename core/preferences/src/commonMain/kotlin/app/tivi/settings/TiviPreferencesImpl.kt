// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.settings.TiviPreferences.Theme
import app.tivi.util.AppCoroutineDispatchers
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.set
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalSettingsApi::class)
@Inject
class TiviPreferencesImpl(
  settings: Lazy<ObservableSettings>,
  private val coroutineScope: ApplicationCoroutineScope,
  private val dispatchers: AppCoroutineDispatchers,
) : TiviPreferences {
  private val settings: ObservableSettings by settings
  private val flowSettings by lazy { settings.value.toFlowSettings(dispatchers.io) }

  override val theme: Preference<Theme> by lazy {
    MappingPreference(KEY_THEME, Theme.SYSTEM, ::getThemeForStorageValue, ::themeToStorageValue)
  }

  override val useDynamicColors: Preference<Boolean> by lazy {
    BooleanPreference(KEY_USE_DYNAMIC_COLORS, true)
  }
  override val useLessData: Preference<Boolean> by lazy {
    BooleanPreference(KEY_DATA_SAVER)
  }
  override val libraryFollowedActive: Preference<Boolean> by lazy {
    BooleanPreference(KEY_LIBRARY_FOLLOWED_ACTIVE)
  }
  override val upNextFollowedOnly: Preference<Boolean> by lazy {
    BooleanPreference(KEY_UPNEXT_FOLLOWED_ONLY)
  }
  override val ignoreSpecials: Preference<Boolean> by lazy {
    BooleanPreference(KEY_IGNORE_SPECIALS, true)
  }
  override val reportAppCrashes: Preference<Boolean> by lazy {
    BooleanPreference(KEY_OPT_IN_CRASH_REPORTING, true)
  }
  override val reportAnalytics: Preference<Boolean> by lazy {
    BooleanPreference(KEY_OPT_IN_ANALYTICS_REPORTING, true)
  }
  override val developerHideArtwork: Preference<Boolean> by lazy {
    BooleanPreference(KEY_DEV_HIDE_ARTWORK)
  }
  override val episodeAiringNotificationsEnabled: Preference<Boolean> by lazy {
    BooleanPreference(KEY_NOTIFICATIONS)
  }

  private inner class BooleanPreference(
    private val key: String,
    override val defaultValue: Boolean = false,
  ) : Preference<Boolean> {
    override suspend fun set(value: Boolean) = withContext(dispatchers.io) {
      settings[key] = value
    }

    override suspend fun get(): Boolean = withContext(dispatchers.io) {
      settings.getBoolean(key, defaultValue)
    }

    override val flow: StateFlow<Boolean> by lazy {
      flowSettings
        .getBooleanFlow(key, defaultValue)
        .stateIn(
          scope = coroutineScope,
          started = SharingStarted.WhileSubscribed(SUBSCRIBED_TIMEOUT),
          initialValue = defaultValue,
        )
    }
  }

  private inner class MappingPreference<V>(
    private val key: String,
    override val defaultValue: V,
    private val toValue: (String) -> V,
    private val fromValue: (V) -> String,
  ) : Preference<V> {
    override suspend fun set(value: V) = withContext(dispatchers.io) {
      settings[key] = fromValue(value)
    }

    override suspend fun get(): V = withContext(dispatchers.io) {
      settings.getStringOrNull(key)?.let(toValue) ?: defaultValue
    }

    override val flow: Flow<V> by lazy {
      flowSettings.getStringOrNullFlow(key)
        .map { it?.let(toValue) ?: defaultValue }
        .shareIn(
          scope = coroutineScope,
          started = SharingStarted.WhileSubscribed(SUBSCRIBED_TIMEOUT),
        )
    }
  }

  private companion object {
    val SUBSCRIBED_TIMEOUT = 20.seconds
  }
}

private fun themeToStorageValue(theme: Theme): String = when (theme) {
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
