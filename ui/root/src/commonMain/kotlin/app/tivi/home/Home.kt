// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.LocalWindowSizeClass
import app.tivi.common.ui.resources.TiviStrings
import app.tivi.screens.DiscoverScreen
import app.tivi.screens.LibraryScreen
import app.tivi.screens.SearchScreen
import app.tivi.screens.UpNextScreen
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.isAtRoot
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveNavigationBar
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveNavigationBarItem
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi

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

  val rootScreen by remember(backstack) {
    derivedStateOf { backstack.last().screen }
  }

  val strings = LocalStrings.current
  val navigationItems = remember(strings) { buildNavigationItems(strings) }

  HazeScaffold(
    bottomBar = {
      if (navigationType == NavigationType.BOTTOM_NAVIGATION) {
        HomeNavigationBar(
          selectedNavigation = rootScreen,
          navigationItems = navigationItems,
          onNavigationSelected = { navigator.resetRootIfDifferent(it, backstack) },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    blurBottomBar = true,
    modifier = modifier,
  ) {
    Row(modifier = Modifier.fillMaxSize()) {
      if (navigationType == NavigationType.RAIL) {
        HomeNavigationRail(
          selectedNavigation = rootScreen,
          navigationItems = navigationItems,
          onNavigationSelected = { navigator.resetRootIfDifferent(it, backstack) },
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
          navigationItems = navigationItems,
          onNavigationSelected = { navigator.resetRoot(it) },
          modifier = Modifier.fillMaxHeight(),
        )
      }

      ContentWithOverlays(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
      ) {
        NavigableCircuitContent(
          navigator = navigator,
          backstack = backstack,
          decoration = remember(navigator) {
            GestureNavigationDecoration(onBackInvoked = navigator::pop)
          },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
private fun HomeNavigationBar(
  selectedNavigation: Screen,
  navigationItems: List<HomeNavigationItem>,
  onNavigationSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  AdaptiveNavigationBar(
    modifier = modifier,
    adaptation = {
      material {
        containerColor = Color.Transparent
      }
      cupertino {
        isTransparent = true
      }
    },
    windowInsets = WindowInsets.navigationBars,
  ) {
    for (item in navigationItems) {
      AdaptiveNavigationBarItem(
        icon = {
          HomeNavigationItemIcon(
            item = item,
            selected = selectedNavigation == item.screen,
          )
        },
        label = { Text(text = item.label) },
        selected = selectedNavigation == item.screen,
        onClick = { onNavigationSelected(item.screen) },
      )
    }
  }
}

@Composable
private fun HomeNavigationRail(
  selectedNavigation: Screen,
  navigationItems: List<HomeNavigationItem>,
  onNavigationSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationRail(modifier = modifier) {
    for (item in navigationItems) {
      NavigationRailItem(
        icon = {
          HomeNavigationItemIcon(
            item = item,
            selected = selectedNavigation == item.screen,
          )
        },
        alwaysShowLabel = false,
        label = { Text(text = item.label) },
        selected = selectedNavigation == item.screen,
        onClick = { onNavigationSelected(item.screen) },
      )
    }
  }
}

@Composable
private fun HomeNavigationDrawer(
  selectedNavigation: Screen,
  navigationItems: List<HomeNavigationItem>,
  onNavigationSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .windowInsetsPadding(WindowInsets.safeContent)
      .padding(16.dp)
      .widthIn(max = 280.dp),
  ) {
    for (item in navigationItems) {
      NavigationDrawerItem(
        icon = {
          Icon(
            imageVector = item.iconImageVector,
            contentDescription = item.contentDescription,
          )
        },
        label = { Text(text = item.label) },
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
        contentDescription = item.contentDescription,
      )
    }
  } else {
    Icon(
      imageVector = item.iconImageVector,
      contentDescription = item.contentDescription,
    )
  }
}

@Immutable
private data class HomeNavigationItem(
  val screen: Screen,
  val label: String,
  val contentDescription: String,
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

private fun buildNavigationItems(strings: TiviStrings): List<HomeNavigationItem> {
  return listOf(
    HomeNavigationItem(
      screen = DiscoverScreen,
      label = strings.discoverTitle,
      contentDescription = strings.cdDiscoverTitle,
      iconImageVector = Icons.Outlined.Weekend,
      selectedImageVector = Icons.Default.Weekend,
    ),
    HomeNavigationItem(
      screen = UpNextScreen,
      label = strings.upnextTitle,
      contentDescription = strings.cdUpnextTitle,
      iconImageVector = Icons.Default.Subscriptions,
    ),
    HomeNavigationItem(
      screen = LibraryScreen,
      label = strings.libraryTitle,
      contentDescription = strings.cdLibraryTitle,
      iconImageVector = Icons.Outlined.VideoLibrary,
      selectedImageVector = Icons.Default.VideoLibrary,
    ),
    HomeNavigationItem(
      screen = SearchScreen,
      label = strings.searchNavigationTitle,
      contentDescription = strings.cdSearchNavigationTitle,
      iconImageVector = Icons.Default.Search,
    ),
  )
}

private fun Navigator.resetRootIfDifferent(
  screen: Screen,
  backstack: SaveableBackStack,
) {
  if (!backstack.isAtRoot || backstack.topRecord?.screen != screen) {
    resetRoot(screen)
  }
}
