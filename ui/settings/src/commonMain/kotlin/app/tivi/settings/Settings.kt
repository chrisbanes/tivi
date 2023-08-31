// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.itemSpacer
import app.tivi.screens.SettingsScreen
import com.moriatsushi.insetsx.systemBars
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

    Scaffold(
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
                windowInsets = WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            )
        },
        contentWindowInsets = WindowInsets.systemBars,
        modifier = modifier,
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            stickyHeader {
                SettingsHeader(LocalStrings.current.settingsUiCategoryTitle)
            }

            item {
                ThemePreference(
                    title = strings.settingsThemeTitle,
                    selected = state.theme,
                    onThemeSelected = { eventSink(SettingsUiEvent.SetTheme(it)) },
                )
            }

            item {
                Divider(Modifier.padding(horizontal = 16.dp))
            }

            if (state.dynamicColorsAvailable) {
                item {
                    CheckboxPreference(
                        title = strings.settingsDynamicColorTitle,
                        summaryOff = strings.settingsDynamicColorSummary,
                        onCheckClicked = { eventSink(SettingsUiEvent.ToggleUseDynamicColors) },
                        checked = state.useDynamicColors,
                    )
                }

                item {
                    Divider(Modifier.padding(horizontal = 16.dp))
                }
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
                SettingsHeader(LocalStrings.current.settingsAboutCategoryTitle)
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

            item {
                Divider(Modifier.padding(horizontal = 16.dp))
            }

            item {
                Preference(
                    title = LocalStrings.current.viewPrivacyPolicy,
                    modifier = Modifier.clickable {
                        eventSink(SettingsUiEvent.NavigatePrivacyPolicy)
                    },
                )
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
private fun SettingsHeader(title: String) {
    Surface {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
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

@Composable
private fun CheckboxPreference(
    checked: Boolean,
    onCheckClicked: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    summaryOff: String? = null,
    summaryOn: String? = null,
) {
    Preference(
        title = title,
        summary = {
            if (summaryOff != null && summaryOn != null) {
                AnimatedContent(checked) { target ->
                    Text(text = if (target) summaryOn else summaryOff)
                }
            } else if (summaryOff != null) {
                Text(text = summaryOff)
            }
        },
        control = {
            Switch(
                checked = checked,
                onCheckedChange = { onCheckClicked() },
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun Preference(
    title: String,
    modifier: Modifier = Modifier,
    summary: (@Composable () -> Unit)? = null,
    control: (@Composable () -> Unit)? = null,
) {
    Surface(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )

                if (summary != null) {
                    ProvideTextStyle(
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        summary()
                    }
                }
            }

            control?.invoke()
        }
    }
}
