// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
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
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.popUntil
import com.slack.circuit.runtime.resetRoot
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.gesturenavigation.GestureNavigationDecoration
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
internal fun Home(
  backStack: SaveableBackStack,
  navigator: Navigator,
  modifier: Modifier = Modifier,
) {
  val windowSizeClass = LocalWindowSizeClass.current
  val navigationType = remember(windowSizeClass) {
    NavigationType.forWindowSizeSize(windowSizeClass)
  }

  val rootScreen by remember(backStack) {
    derivedStateOf { backStack.last().screen }
  }

  val strings = LocalStrings.current
  val navigationItems = remember(strings) { buildNavigationItems(strings) }

  val hazeState = remember { HazeState() }

  HazeScaffold(
    bottomBar = {
      if (navigationType == NavigationType.BOTTOM_NAVIGATION) {
        Box {
          HomeNavigationBar(
            selectedNavigation = rootScreen,
            navigationItems = navigationItems,
            onNavigationSelected = {
              navigator.resetRootIfDifferent(it, saveState = true, restoreState = true)
            },
            modifier = Modifier
              .padding(horizontal = 24.dp)
              .padding(bottom = 8.dp)
              .windowInsetsPadding(WindowInsets.navigationBars)
              .hazeChild(
                state = hazeState,
                style = HazeMaterials.regular(),
                shape = MaterialTheme.shapes.extraLarge,
              )
              .fillMaxWidth(),
          )
        }
      }
    },
    hazeState = hazeState,
    modifier = modifier,
  ) {
    Row(modifier = Modifier.fillMaxSize()) {
      if (navigationType == NavigationType.RAIL) {
        HomeNavigationRail(
          selectedNavigation = rootScreen,
          navigationItems = navigationItems,
          onNavigationSelected = {
            navigator.resetRootIfDifferent(it, saveState = true, restoreState = true)
          },
          modifier = Modifier.fillMaxHeight(),
        )

        VerticalDivider()
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
          backStack = backStack,
          decoration = remember(navigator) {
            GestureNavigationDecoration(onBackInvoked = navigator::pop)
          },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
fun FloatingNavigationBar(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.extraLarge,
  containerColor: Color = NavigationBarDefaults.containerColor,
  contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
  tonalElevation: Dp = NavigationBarDefaults.Elevation,
  content: @Composable RowScope.() -> Unit,
) {
  Surface(
    color = containerColor,
    contentColor = contentColor,
    tonalElevation = tonalElevation,
    shape = shape,
    border = BorderStroke(
      width = 0.5.dp,
      brush = Brush.verticalGradient(
        colors = listOf(
          MaterialTheme.colorScheme.surfaceVariant,
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
      ),
    ),
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 8.dp)
        .fillMaxWidth()
        .height(80.dp)
        .selectableGroup(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      content = content,
    )
  }
}

@Composable
private fun HomeNavigationBar(
  selectedNavigation: Screen,
  navigationItems: List<HomeNavigationItem>,
  onNavigationSelected: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  FloatingNavigationBar(
    modifier = modifier,
    containerColor = Color.Transparent,
  ) {
    val colors = NavigationBarItemDefaults.colors(
      selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
      selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
      unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
      unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    )

    for (item in navigationItems) {
      val isSelected = selectedNavigation == item.screen
      val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = spring(
          stiffness = Spring.StiffnessLow,
          dampingRatio = Spring.DampingRatioLowBouncy,
        ),
      )

      NavigationBarItem(
        icon = {
          HomeNavigationItemIcon(
            item = item,
            selected = selectedNavigation == item.screen,
          )
        },
        label = { Text(text = item.label) },
        selected = selectedNavigation == item.screen,
        colors = colors,
        onClick = { onNavigationSelected(item.screen) },
        modifier = Modifier
          .testTag(item.tag)
          .graphicsLayer {
            scaleX = scale
            scaleY = scale
          },
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
  val tag: String,
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
      tag = "home_nav_discover",
      contentDescription = strings.cdDiscoverTitle,
      iconImageVector = Icons.Outlined.Weekend,
      selectedImageVector = Icons.Default.Weekend,
    ),
    HomeNavigationItem(
      screen = UpNextScreen,
      label = strings.upnextTitle,
      tag = "home_nav_upnext",
      contentDescription = strings.cdUpnextTitle,
      iconImageVector = Icons.Default.Subscriptions,
    ),
    HomeNavigationItem(
      screen = LibraryScreen,
      label = strings.libraryTitle,
      tag = "home_nav_library",
      contentDescription = strings.cdLibraryTitle,
      iconImageVector = Icons.Outlined.VideoLibrary,
      selectedImageVector = Icons.Default.VideoLibrary,
    ),
    HomeNavigationItem(
      screen = SearchScreen,
      label = strings.searchNavigationTitle,
      tag = "home_nav_search",
      contentDescription = strings.cdSearchNavigationTitle,
      iconImageVector = Icons.Default.Search,
    ),
  )
}

private fun Navigator.resetRootIfDifferent(
  screen: Screen,
  saveState: Boolean = false,
  restoreState: Boolean = false,
) {
  val backStack = peekBackStack()

  if (backStack.lastOrNull() == screen) {
    Snapshot.withMutableSnapshot {
      popUntil { peekBackStack().size == 1 }
    }
  } else {
    resetRoot(screen, saveState, restoreState)
  }
}
