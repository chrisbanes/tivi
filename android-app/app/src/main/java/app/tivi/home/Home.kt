/*
 * Copyright 2020 Google LLC
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

@file:OptIn(ExperimentalMaterial3Api::class)

package app.tivi.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import app.tivi.common.ui.resources.MR
import app.tivi.core.analytics.Analytics
import app.tivi.screens.DiscoverScreen
import app.tivi.screens.LibraryScreen
import app.tivi.screens.SearchScreen
import app.tivi.screens.UpNextScreen
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.runtime.Screen
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource

@Suppress("UNUSED_PARAMETER", "UNUSED_ANONYMOUS_PARAMETER")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun Home(
    analytics: Analytics,
    circuitConfig: CircuitConfig,
    onOpenSettings: () -> Unit,
) { // Launch an effect to track changes to the current back stack entry, and push them
    // as a screen views to analytics
//    LaunchedEffect(navController, analytics) {
//        navController.currentBackStackEntryFlow.collect { entry ->
//            analytics.trackScreenView(
//                label = entry.debugLabel,
//                route = entry.destination.route,
//                arguments = entry.arguments,
//            )
//        }
//    }

    val configuration = LocalConfiguration.current
    val useBottomNavigation = configuration.smallestScreenWidthDp < 600

    Scaffold(
        bottomBar = {
            if (useBottomNavigation) {
                HomeNavigationBar(
                    selectedNavigation = DiscoverScreen, // FIXME
                    onNavigationSelected = { selected ->
//                        navController.navigate(selected.route) {
//                            launchSingleTop = true
//                            restoreState = true
//
//                            popUpTo(navController.graph.findStartDestination().id) {
//                                saveState = true
//                            }
//                        }
                    },
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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.statusBars), // We let content handle the status bar
        modifier = Modifier.semantics {
            // Enables testTag -> UiAutomator resource id
            // See https://developer.android.com/jetpack/compose/testing#uiautomator-interop
            testTagsAsResourceId = true
        },
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (!useBottomNavigation) {
                HomeNavigationRail(
                    selectedNavigation = DiscoverScreen, // FIXME
                    onNavigationSelected = { selected ->
//                        navController.navigate(selected.route) {
//                            launchSingleTop = true
//                            restoreState = true
//
//                            popUpTo(navController.graph.findStartDestination().id) {
//                                saveState = true
//                            }
//                        }
                    },
                    modifier = Modifier.fillMaxHeight(),
                )

                Divider(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                )
            }

            CircuitCompositionLocals(circuitConfig) {
                CircuitContent(
                    screen = DiscoverScreen,
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
    NavigationBar(modifier = modifier) {
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
private fun HomeNavigationItemIcon(item: HomeNavigationItem, selected: Boolean) {
    val painter = when (item) {
        is HomeNavigationItem.ResourceIcon -> painterResource(item.iconResId)
        is HomeNavigationItem.ImageVectorIcon -> rememberVectorPainter(item.iconImageVector)
    }
    val selectedPainter = when (item) {
        is HomeNavigationItem.ResourceIcon -> item.selectedIconResId?.let { painterResource(it) }
        is HomeNavigationItem.ImageVectorIcon -> item.selectedImageVector?.let { rememberVectorPainter(it) }
    }

    if (selectedPainter != null) {
        Crossfade(targetState = selected) {
            Icon(
                painter = if (it) selectedPainter else painter,
                contentDescription = stringResource(item.contentDescriptionResource),
            )
        }
    } else {
        Icon(
            painter = painter,
            contentDescription = stringResource(item.contentDescriptionResource),
        )
    }
}

private sealed class HomeNavigationItem(
    val screen: Screen,
    val labelResource: StringResource,
    val contentDescriptionResource: StringResource,
) {
    class ResourceIcon(
        screen: Screen,
        labelResource: StringResource,
        contentDescriptionResource: StringResource,
        @DrawableRes val iconResId: Int,
        @DrawableRes val selectedIconResId: Int? = null,
    ) : HomeNavigationItem(screen, labelResource, contentDescriptionResource)

    class ImageVectorIcon(
        screen: Screen,
        labelResource: StringResource,
        contentDescriptionResource: StringResource,
        val iconImageVector: ImageVector,
        val selectedImageVector: ImageVector? = null,
    ) : HomeNavigationItem(screen, labelResource, contentDescriptionResource)
}

private val HomeNavigationItems = listOf(
    HomeNavigationItem.ImageVectorIcon(
        screen = DiscoverScreen,
        labelResource = MR.strings.discover_title,
        contentDescriptionResource = MR.strings.cd_discover_title,
        iconImageVector = Icons.Outlined.Weekend,
        selectedImageVector = Icons.Default.Weekend,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = UpNextScreen,
        labelResource = MR.strings.upnext_title,
        contentDescriptionResource = MR.strings.cd_upnext_title,
        iconImageVector = Icons.Default.Subscriptions,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = LibraryScreen,
        labelResource = MR.strings.library_title,
        contentDescriptionResource = MR.strings.cd_library_title,
        iconImageVector = Icons.Outlined.VideoLibrary,
        selectedImageVector = Icons.Default.VideoLibrary,
    ),
    HomeNavigationItem.ImageVectorIcon(
        screen = SearchScreen,
        labelResource = MR.strings.search_navigation_title,
        contentDescriptionResource = MR.strings.cd_search_navigation_title,
        iconImageVector = Icons.Default.Search,
    ),
)
