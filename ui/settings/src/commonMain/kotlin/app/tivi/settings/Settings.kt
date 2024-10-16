// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.ui.ArrowBackForPlatform
import app.tivi.common.compose.ui.CheckboxPreference
import app.tivi.common.compose.ui.Preference
import app.tivi.common.compose.ui.PreferenceDivider
import app.tivi.common.compose.ui.PreferenceHeader
import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.account_delete_account_cta
import app.tivi.common.ui.resources.account_delete_confirmation_message
import app.tivi.common.ui.resources.account_delete_confirmation_title
import app.tivi.common.ui.resources.cd_navigate_up
import app.tivi.common.ui.resources.developer_settings_title
import app.tivi.common.ui.resources.dialog_no
import app.tivi.common.ui.resources.dialog_yes
import app.tivi.common.ui.resources.settings_about_category_title
import app.tivi.common.ui.resources.settings_account_category_title
import app.tivi.common.ui.resources.settings_analytics_data_collection_summary
import app.tivi.common.ui.resources.settings_analytics_data_collection_title
import app.tivi.common.ui.resources.settings_app_version
import app.tivi.common.ui.resources.settings_app_version_summary
import app.tivi.common.ui.resources.settings_crash_data_collection_summary
import app.tivi.common.ui.resources.settings_crash_data_collection_title
import app.tivi.common.ui.resources.settings_data_saver_summary_off
import app.tivi.common.ui.resources.settings_data_saver_summary_on
import app.tivi.common.ui.resources.settings_data_saver_title
import app.tivi.common.ui.resources.settings_dynamic_color_summary
import app.tivi.common.ui.resources.settings_dynamic_color_title
import app.tivi.common.ui.resources.settings_ignore_specials_summary
import app.tivi.common.ui.resources.settings_ignore_specials_title
import app.tivi.common.ui.resources.settings_notifications_airing_episodes_summary
import app.tivi.common.ui.resources.settings_notifications_airing_episodes_title
import app.tivi.common.ui.resources.settings_notifications_category_title
import app.tivi.common.ui.resources.settings_open_source
import app.tivi.common.ui.resources.settings_open_source_summary
import app.tivi.common.ui.resources.settings_privacy_category_title
import app.tivi.common.ui.resources.settings_theme_title
import app.tivi.common.ui.resources.settings_title
import app.tivi.common.ui.resources.settings_ui_category_title
import app.tivi.common.ui.resources.view_privacy_policy
import app.tivi.screens.SettingsScreen
import app.tivi.util.launchOrThrow
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import com.slack.circuitx.overlays.DialogResult
import com.slack.circuitx.overlays.alertDialogOverlay
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@Inject
class SettingsUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is SettingsScreen -> {
      ui<SettingsUiState> { state, modifier ->
        Settings(state, modifier)
      }
    }

    else -> null
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun Settings(
  state: SettingsUiState,
  modifier: Modifier = Modifier,
) {
  // Need to extract the eventSink out to a local val, so that the Compose Compiler
  // treats it as stable. See: https://issuetracker.google.com/issues/256100927
  val eventSink = state.eventSink

  ContentWithOverlays {
    HazeScaffold(
      topBar = {
        TopAppBar(
          title = { Text(stringResource(Res.string.settings_title)) },
          navigationIcon = {
            IconButton(onClick = { eventSink(SettingsUiEvent.NavigateUp) }) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackForPlatform,
                contentDescription = stringResource(Res.string.cd_navigate_up),
              )
            }
          },
        )
      },
      modifier = modifier,
    ) { contentPadding ->
      val overlayHost = LocalOverlayHost.current
      val scope = rememberCoroutineScope()

      LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxWidth(),
      ) {
        stickyHeader {
          PreferenceHeader(stringResource(Res.string.settings_ui_category_title))
        }

        item {
          ThemePreference(
            title = stringResource(Res.string.settings_theme_title),
            selected = state.theme,
            onThemeSelected = { eventSink(SettingsUiEvent.SetTheme(it)) },
          )
        }

        item { PreferenceDivider() }

        if (state.dynamicColorsAvailable) {
          item {
            CheckboxPreference(
              title = stringResource(Res.string.settings_dynamic_color_title),
              summaryOff = stringResource(Res.string.settings_dynamic_color_summary),
              onCheckClicked = { eventSink(SettingsUiEvent.ToggleUseDynamicColors) },
              checked = state.useDynamicColors,
            )
          }

          item { PreferenceDivider() }
        }

        item {
          CheckboxPreference(
            title = stringResource(Res.string.settings_data_saver_title),
            summaryOff = stringResource(Res.string.settings_data_saver_summary_off),
            summaryOn = stringResource(Res.string.settings_data_saver_summary_on),
            onCheckClicked = { eventSink(SettingsUiEvent.ToggleUseLessData) },
            checked = state.useLessData,
          )
        }

        item { PreferenceDivider() }

        item {
          CheckboxPreference(
            title = stringResource(Res.string.settings_ignore_specials_title),
            summaryOff = stringResource(Res.string.settings_ignore_specials_summary),
            onCheckClicked = { eventSink(SettingsUiEvent.ToggleIgnoreSpecials) },
            checked = state.ignoreSpecials,
          )
        }

        itemSpacer(24.dp)

        stickyHeader {
          PreferenceHeader(stringResource(Res.string.settings_notifications_category_title))
        }

        item {
          CheckboxPreference(
            title = stringResource(Res.string.settings_notifications_airing_episodes_title),
            summaryOff = stringResource(Res.string.settings_notifications_airing_episodes_summary),
            onCheckClicked = { eventSink(SettingsUiEvent.ToggleAiringEpisodeNotificationsEnabled) },
            checked = state.airingEpisodeNotificationsEnabled,
          )
        }

        itemSpacer(24.dp)

        if (state.isLoggedIn) {
          stickyHeader {
            PreferenceHeader(stringResource(Res.string.settings_account_category_title))
          }

          if (state.showDeleteAccount) {
            item {
              Preference(
                title = stringResource(Res.string.account_delete_account_cta),
                onClick = {
                  scope.launchOrThrow {
                    val result = overlayHost.showDeleteAccountConfirmationDialog()
                    if (result == DialogResult.Confirm) {
                      eventSink(SettingsUiEvent.DeleteAccount)
                    }
                  }
                },
              )
            }
          }

          itemSpacer(24.dp)
        }

        stickyHeader {
          PreferenceHeader(stringResource(Res.string.settings_privacy_category_title))
        }

        item {
          Preference(
            title = stringResource(Res.string.view_privacy_policy),
            onClick = { eventSink(SettingsUiEvent.NavigatePrivacyPolicy) },
          )
        }

        item { PreferenceDivider() }

        item {
          CheckboxPreference(
            title = stringResource(Res.string.settings_crash_data_collection_title),
            summaryOff = stringResource(Res.string.settings_crash_data_collection_summary),
            onCheckClicked = { eventSink(SettingsUiEvent.ToggleCrashDataReporting) },
            checked = state.crashDataReportingEnabled,
          )
        }

        item { PreferenceDivider() }

        item {
          CheckboxPreference(
            title = stringResource(Res.string.settings_analytics_data_collection_title),
            summaryOff = stringResource(Res.string.settings_analytics_data_collection_summary),
            onCheckClicked = { eventSink(SettingsUiEvent.ToggleAnalyticsDataReporting) },
            checked = state.analyticsDataReportingEnabled,
          )
        }

        itemSpacer(24.dp)

        stickyHeader {
          PreferenceHeader(stringResource(Res.string.settings_about_category_title))
        }

        item {
          Preference(
            title = stringResource(Res.string.settings_app_version),
            summary = {
              Text(
                text = stringResource(
                  Res.string.settings_app_version_summary,
                  state.applicationInfo.versionName,
                  state.applicationInfo.versionCode,
                ),
              )
            },
          )
        }

        if (state.openSourceLicenseAvailable) {
          item { PreferenceDivider() }

          item {
            Preference(
              title = stringResource(Res.string.settings_open_source),
              summary = {
                Text(stringResource(Res.string.settings_open_source_summary))
              },
              onClick = { eventSink(SettingsUiEvent.NavigateOpenSource) },
            )
          }
        }

        if (state.showDeveloperSettings) {
          item { PreferenceDivider() }

          item {
            Preference(
              title = stringResource(Res.string.developer_settings_title),
              onClick = { eventSink(SettingsUiEvent.NavigateDeveloperSettings) },
            )
          }
        }

        itemSpacer(16.dp)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
private suspend fun OverlayHost.showDeleteAccountConfirmationDialog(): DialogResult {
  return show(
    alertDialogOverlay(
      title = { Text(stringResource(Res.string.account_delete_confirmation_title)) },
      text = { Text(stringResource(Res.string.account_delete_confirmation_message)) },
      confirmButton = { onClick ->
        Button(
          onClick = onClick,
          content = { Text(stringResource(Res.string.dialog_yes)) },
        )
      },
      dismissButton = { onClick ->
        Button(
          onClick = onClick,
          content = { Text(stringResource(Res.string.dialog_no)) },
        )
      },
    ),
  )
}

@Composable
private fun ThemePreference(
  selected: TiviPreferences.Theme,
  onThemeSelected: (TiviPreferences.Theme) -> Unit,
  title: String,
  modifier: Modifier = Modifier,
) {
  Preference(
    title = title,
    control = {
      Row(Modifier.selectableGroup()) {
        ThemeButton(
          icon = Icons.Default.AutoMode,
          onClick = { onThemeSelected(TiviPreferences.Theme.SYSTEM) },
          isSelected = selected == TiviPreferences.Theme.SYSTEM,
        )

        ThemeButton(
          icon = Icons.Default.LightMode,
          onClick = { onThemeSelected(TiviPreferences.Theme.LIGHT) },
          isSelected = selected == TiviPreferences.Theme.LIGHT,
        )

        ThemeButton(
          icon = Icons.Default.DarkMode,
          onClick = { onThemeSelected(TiviPreferences.Theme.DARK) },
          isSelected = selected == TiviPreferences.Theme.DARK,
        )
      }
    },
    modifier = modifier,
  )
}

@Composable
private fun ThemeButton(
  isSelected: Boolean,
  icon: ImageVector,
  onClick: () -> Unit,
) {
  FilledIconToggleButton(
    checked = isSelected,
    onCheckedChange = { onClick() },
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
    )
  }
}
