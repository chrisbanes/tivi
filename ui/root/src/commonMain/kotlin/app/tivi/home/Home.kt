// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.LocalWindowSizeClass
import app.tivi.common.ui.resources.MR
import app.tivi.screens.DiscoverScreen
import app.tivi.screens.LibraryScreen
import app.tivi.screens.SearchScreen
import app.tivi.screens.UpNextScreen
import com.moriatsushi.insetsx.navigationBars
import com.moriatsushi.insetsx.safeContentPadding
import com.moriatsushi.insetsx.statusBars
import com.moriatsushi.insetsx.systemBars
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.screen
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Home(
    backstack: SaveableBackStack,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val navigationType = remember(windowSizeClass) {
        NavigationType.forWindowSizeSize(windowSizeClass)
    }

    val rootScreen by remember {
        derivedStateOf { backstack.last().screen }
    }

    Scaffold(
        bottomBar = {
            if (navigationType == NavigationType.BOTTOM_NAVIGATION) {
                HomeNavigationBar(
                    selectedNavigation = rootScreen,
                    onNavigationSelected = { navigator.resetRoot(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Spacer(
                    Modifier
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                        .fillMaxWidth(),
                )
            }
        },
        // We let content handle the status bar
        contentWindowInsets = WindowInsets.systemBars.exclude(WindowInsets.statusBars),
        modifier = modifier,
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (navigationType == NavigationType.RAIL) {
                HomeNavigationRail(
                    selectedNavigation = rootScreen,
                    onNavigationSelected = { navigator.resetRoot(it) },
                    modifier = Modifier.fillMaxHeight(),
                )

                Divider(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                )
            } else if (navigationType == NavigationType.PERMANENT_DRAWER) {
                HomeNavigationDrawer(
                    selectedNavigation = rootScreen,
                    onNavigationSelected = { navigator.resetRoot(it) },
                    modifier = Modifier.fillMaxHeight(),
                )
            }

            ContentWithOverlays {
                NavigableCircuitContent(
                    navigator = navigator,
                    backstack = backstack,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
internal fun HomeNavigationBar(
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets.navigationBars,
    ) {
        for (item in HomeNavigationItems) {
            NavigationBarItem(
                icon = {
                    HomeNavigationItemIcon(
                        item = item,
                        selected = selectedNavigation == item.screen,
                    )
                },
                label = { Text(text = stringResource(item.labelResource)) },
                selected = selectedNavigation == item.screen,
                onClick = { onNavigationSelected(item.screen) },
            )
        }
    }
}

@Composable
internal fun HomeNavigationRail(
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier = modifier) {
        for (item in HomeNavigationItems) {
            NavigationRailItem(
                icon = {
                    HomeNavigationItemIcon(
                        item = item,
                        selected = selectedNavigation == item.screen,
                    )
                },
                alwaysShowLabel = false,
                label = { Text(text = stringResource(item.labelResource)) },
                selected = selectedNavigation == item.screen,
                onClick = { onNavigationSelected(item.screen) },
            )
        }
    }
}

@Composable
internal fun HomeNavigationDrawer(
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .safeContentPadding()
            .padding(16.dp)
            .widthIn(max = 280.dp),
    ) {
        for (item in HomeNavigationItems) {
            @OptIn(ExperimentalMaterial3Api::class)
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = item.iconImageVector,
                        contentDescription = stringResource(item.contentDescriptionResource),
                    )
                },
                label = { Text(text = stringResource(item.labelResource)) },
                selected = selectedNavigation == item.screen,
                onClick = { onNavigationSelected(item.screen) },
            )
        }
    }
}

@Composable
private fun HomeNavigationItemIcon(item: HomeNavigationItem, selected: Boolean) {
    if (item.selectedImageVector != null) {
        Crossfade(targetState = selected) { s ->
            Icon(
                imageVector = if (s) item.selectedImageVector else item.iconImageVector,
                contentDescription = stringResource(item.contentDescriptionResource),
            )
        }
    } else {
        Icon(
            imageVector = item.iconImageVector,
            contentDescription = stringResource(item.contentDescriptionResource),
        )
    }
}

@Immutable
private data class HomeNavigationItem(
    val screen: Screen,
    val labelResource: StringResource,
    val contentDescriptionResource: StringResource,
    val iconImageVector: ImageVector,
    val selectedImageVector: ImageVector? = null,
)

internal enum class NavigationType {
    BOTTOM_NAVIGATION,
    RAIL,
    PERMANENT_DRAWER,
    ;

    companion object {
        fun forWindowSizeSize(windowSizeClass: WindowSizeClass): NavigationType = when {
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact -> BOTTOM_NAVIGATION
            windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact -> BOTTOM_NAVIGATION
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium -> RAIL
            else -> PERMANENT_DRAWER
        }
    }
}

private val HomeNavigationItems by lazy {
    listOf(
        HomeNavigationItem(
            screen = DiscoverScreen,
            labelResource = MR.strings.discover_title,
            contentDescriptionResource = MR.strings.cd_discover_title,
            iconImageVector = Icons.Outlined.Weekend,
            selectedImageVector = Icons.Default.Weekend,
        ),
        HomeNavigationItem(
            screen = UpNextScreen,
            labelResource = MR.strings.upnext_title,
            contentDescriptionResource = MR.strings.cd_upnext_title,
            iconImageVector = Icons.Default.Subscriptions,
        ),
        HomeNavigationItem(
            screen = LibraryScreen,
            labelResource = MR.strings.library_title,
            contentDescriptionResource = MR.strings.cd_library_title,
            iconImageVector = Icons.Outlined.VideoLibrary,
            selectedImageVector = Icons.Default.VideoLibrary,
        ),
        HomeNavigationItem(
            screen = SearchScreen,
            labelResource = MR.strings.search_navigation_title,
            contentDescriptionResource = MR.strings.cd_search_navigation_title,
            iconImageVector = Icons.Default.Search,
        ),
    )
}
