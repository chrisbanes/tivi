// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import app.tivi.common.compose.ColorExtractor
import app.tivi.common.compose.LocalColorExtractor
import app.tivi.common.compose.LocalPreferences
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.LocalWindowSizeClass
import app.tivi.common.compose.ProvideStrings
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.core.analytics.Analytics
import app.tivi.navigation.DeepLinker
import app.tivi.navigation.LaunchDeepLinker
import app.tivi.navigation.LocalNavigator
import app.tivi.screens.TiviScreen
import app.tivi.screens.UrlScreen
import app.tivi.settings.TiviPreferences
import app.tivi.util.Logger
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.retained.LocalRetainedStateRegistry
import com.slack.circuit.retained.continuityRetainedStateRegistry
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Inject

interface TiviContent {
  @Composable
  fun Content(
    backstack: SaveableBackStack,
    navigator: Navigator,
    onOpenUrl: (String) -> Boolean,
    modifier: Modifier,
  )
}

@Inject
class DefaultTiviContent(
  private val rootViewModel: (CoroutineScope) -> RootViewModel,
  private val circuit: Circuit,
  private val analytics: Analytics,
  private val tiviDateFormatter: TiviDateFormatter,
  private val tiviTextCreator: TiviTextCreator,
  private val preferences: TiviPreferences,
  private val imageLoader: ImageLoader,
  private val colorExtractor: ColorExtractor,
  private val deepLinker: DeepLinker,
  private val logger: Logger,
) : TiviContent {

  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalCoilApi::class)
  @Composable
  override fun Content(
    backstack: SaveableBackStack,
    navigator: Navigator,
    onOpenUrl: (String) -> Boolean,
    modifier: Modifier,
  ) {
    val coroutineScope = rememberCoroutineScope()
    remember { rootViewModel(coroutineScope) }

    val tiviNavigator: Navigator = remember(navigator) {
      TiviNavigator(navigator, backstack, onOpenUrl, logger)
    }

    LaunchDeepLinker(deepLinker = deepLinker, navigator = navigator)

    // Launch an effect to track changes to the current back stack entry, and push them
    // as a screen views to analytics
    LaunchedEffect(backstack.topRecord) {
      val topScreen = backstack.topRecord?.screen as? TiviScreen
      analytics.trackScreenView(
        name = topScreen?.name ?: "unknown screen",
        arguments = topScreen?.arguments,
      )
    }

    setSingletonImageLoaderFactory { imageLoader }

    ProvideStrings {
      CompositionLocalProvider(
        LocalNavigator provides tiviNavigator,
        LocalTiviDateFormatter provides tiviDateFormatter,
        LocalTiviTextCreator provides tiviTextCreator,
        LocalColorExtractor provides colorExtractor,
        LocalPreferences provides preferences,
        LocalWindowSizeClass provides calculateWindowSizeClass(),
        LocalRetainedStateRegistry provides continuityRetainedStateRegistry(),
      ) {
        CircuitCompositionLocals(circuit) {
          TiviTheme {
            Home(
              backStack = backstack,
              navigator = tiviNavigator,
              modifier = modifier,
            )
          }
        }
      }
    }
  }
}

private class TiviNavigator(
  private val navigator: Navigator,
  private val backStack: SaveableBackStack,
  private val onOpenUrl: (String) -> Boolean,
  private val logger: Logger,
) : Navigator {
  override fun goTo(screen: Screen): Boolean {
    logger.d { "goTo. Screen: $screen. Current stack: ${backStack.toList()}" }

    if (screen is UrlScreen && onOpenUrl(screen.url)) {
      return true
    }
    return navigator.goTo(screen)
  }

  override fun pop(result: PopResult?): Screen? {
    logger.d { "pop. Current stack: ${backStack.toList()}" }
    return navigator.pop(result)
  }

  override fun resetRoot(
    newRoot: Screen,
    saveState: Boolean,
    restoreState: Boolean,
  ): ImmutableList<Screen> {
    logger.d { "resetRoot: newRoot:$newRoot. Current stack: ${backStack.toList()}" }
    return navigator.resetRoot(newRoot, saveState, restoreState)
  }

  override fun peek(): Screen? = navigator.peek()

  override fun peekBackStack(): ImmutableList<Screen> = navigator.peekBackStack()
}
