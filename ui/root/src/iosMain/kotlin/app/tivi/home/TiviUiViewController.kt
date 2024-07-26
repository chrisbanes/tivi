// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.platform.AccessibilityDebugLogger
import androidx.compose.ui.platform.AccessibilitySyncOptions
import androidx.compose.ui.window.ComposeUIViewController
import app.tivi.app.ApplicationInfo
import app.tivi.screens.DiscoverScreen
import app.tivi.util.Logger
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIViewController

typealias TiviUiViewController = () -> UIViewController

private const val ENABLE_A11Y_LOGGING = false

@OptIn(ExperimentalComposeApi::class)
@Inject
@Suppress("ktlint:standard:function-naming")
fun TiviUiViewController(
  tiviContent: TiviContent,
  logger: Logger,
  applicationInfo: ApplicationInfo,
): UIViewController = ComposeUIViewController(
  configure = {
    val a11yLogger = if (ENABLE_A11Y_LOGGING) {
      object : AccessibilityDebugLogger {
        override fun log(message: Any?) {
          logger.d { "AccessibilityDebugLogger: $message" }
        }
      }
    } else {
      null
    }

    accessibilitySyncOptions = when {
      applicationInfo.debugBuild -> AccessibilitySyncOptions.Always(a11yLogger)
      else -> AccessibilitySyncOptions.WhenRequiredByAccessibilityServices(a11yLogger)
    }
  },
) {
  val backstack = rememberSaveableBackStack(listOf(DiscoverScreen))
  val navigator = rememberCircuitNavigator(backstack, onRootPop = { /* no-op */ })
  val uiViewController = LocalUIViewController.current

  tiviContent.Content(
    backstack = backstack,
    navigator = navigator,
    onOpenUrl = { url ->
      val safari = SFSafariViewController(NSURL(string = url))
      uiViewController.presentViewController(safari, animated = true, completion = null)
      true
    },
    modifier = Modifier,
  )
}
