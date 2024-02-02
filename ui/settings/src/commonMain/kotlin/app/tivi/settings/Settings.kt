// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.ui.CheckboxPreference
import app.tivi.common.compose.ui.Preference
import app.tivi.common.compose.ui.PreferenceDivider
import app.tivi.common.compose.ui.PreferenceHeader
import app.tivi.common.compose.ui.TopAppBar
import app.tivi.screens.SettingsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject

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

  val strings = LocalStrings.current

  HazeScaffold(
    topBar = {
      TopAppBar(
        title = { Text(strings.settingsTitle) },
        navigationIcon = {
          IconButton(onClick = { eventSink(SettingsUiEvent.NavigateUp) }) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = LocalStrings.current.cdNavigateUp,
            )
          }
        },
      )
    },
    modifier = modifier,
  ) { contentPadding ->
    LazyColumn(
      contentPadding = contentPadding,
      modifier = Modifier.fillMaxWidth(),
    ) {
      stickyHeader {
        PreferenceHeader(LocalStrings.current.settingsUiCategoryTitle)
      }

      item {
        ThemePreference(
          title = strings.settingsThemeTitle,
          selected = state.theme,
          onThemeSelected = { eventSink(SettingsUiEvent.SetTheme(it)) },
        )
      }

      item { PreferenceDivider() }

      if (state.dynamicColorsAvailable) {
        item {
          CheckboxPreference(
            title = strings.settingsDynamicColorTitle,
            summaryOff = strings.settingsDynamicColorSummary,
            onCheckClicked = { eventSink(SettingsUiEvent.ToggleUseDynamicColors) },
            checked = state.useDynamicColors,
          )
        }

        item { PreferenceDivider() }
      }

      item {
        CheckboxPreference(
          title = strings.settingsDataSaverTitle,
          summaryOff = strings.settingsDataSaverSummaryOff,
          summaryOn = strings.settingsDataSaverSummaryOn,
          onCheckClicked = { eventSink(SettingsUiEvent.ToggleUseLessData) },
          checked = state.useLessData,
        )
      }

      item { PreferenceDivider() }

      item {
        CheckboxPreference(
          title = strings.settingsIgnoreSpecialsTitle,
          summaryOff = strings.settingsIgnoreSpecialsSummary,
          onCheckClicked = { eventSink(SettingsUiEvent.ToggleIgnoreSpecials) },
          checked = state.ignoreSpecials,
        )
      }

      itemSpacer(24.dp)

      stickyHeader {
        PreferenceHeader(LocalStrings.current.settingsPrivacyCategoryTitle)
      }

      item {
        Preference(
          title = LocalStrings.current.viewPrivacyPolicy,
          modifier = Modifier.clickable {
            eventSink(SettingsUiEvent.NavigatePrivacyPolicy)
          },
        )
      }

      item { PreferenceDivider() }

      item {
        CheckboxPreference(
          title = strings.settingsCrashDataCollectionTitle,
          summaryOff = strings.settingsCrashDataCollectionSummary,
          onCheckClicked = { eventSink(SettingsUiEvent.ToggleCrashDataReporting) },
          checked = state.crashDataReportingEnabled,
        )
      }

      item { PreferenceDivider() }

      item {
        CheckboxPreference(
          title = strings.settingsAnalyticsDataCollectionTitle,
          summaryOff = strings.settingsAnalyticsDataCollectionSummary,
          onCheckClicked = { eventSink(SettingsUiEvent.ToggleAnalyticsDataReporting) },
          checked = state.analyticsDataReportingEnabled,
        )
      }

      itemSpacer(24.dp)

      stickyHeader {
        PreferenceHeader(LocalStrings.current.settingsAboutCategoryTitle)
      }

      item {
        Preference(
          title = LocalStrings.current.settingsAppVersion,
          summary = {
            Text(
              text = LocalStrings.current.settingsAppVersionSummary(
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
            title = LocalStrings.current.settingsOpenSource,
            summary = {
              Text(LocalStrings.current.settingsOpenSourceSummary)
            },
            modifier = Modifier.clickable {
              eventSink(SettingsUiEvent.NavigateOpenSource)
            },
          )
        }
      }

      if (state.showDeveloperSettings) {
        item { PreferenceDivider() }

        item {
          Preference(
            title = LocalStrings.current.developerSettingsTitle,
            modifier = Modifier.clickable {
              eventSink(SettingsUiEvent.NavigateDeveloperSettings)
            },
          )
        }
      }
    }
  }
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
