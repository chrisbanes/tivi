// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.tivi.ContentViewSetter
import app.tivi.TiviActivity
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.LocalWindowSizeClass
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.shouldUseDynamicColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.core.analytics.Analytics
import app.tivi.data.traktauth.LoginToTraktInteractor
import app.tivi.data.traktauth.TraktAuthActivityComponent
import app.tivi.inject.ActivityComponent
import app.tivi.inject.ActivityScope
import app.tivi.inject.AndroidApplicationComponent
import app.tivi.overlays.LocalNavigator
import app.tivi.screens.DiscoverScreen
import app.tivi.screens.SettingsScreen
import app.tivi.screens.TiviScreen
import app.tivi.settings.SettingsActivity
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.foundation.screen
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

class MainActivity : TiviActivity() {

    private lateinit var component: MainActivityComponent

    private val viewModel: MainActivityViewModel by viewModels {
        viewModelFactory {
            addInitializer(MainActivityViewModel::class) { component.viewModel() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component = MainActivityComponent::class.create(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get the viewModel, so it is started and 'running'
        viewModel

        val composeView = ComposeView(this).apply {
            setContent {
                CompositionLocalProvider(
                    LocalImageLoader provides component.imageLoader,
                    LocalTiviDateFormatter provides component.tiviDateFormatter,
                    LocalTiviTextCreator provides component.textCreator,
                ) {
                    CircuitCompositionLocals(component.circuitConfig) {
                        TiviContent(
                            analytics = component.analytics,
                            preferences = component.preferences,
                            onOpenSettings = {
                                context.startActivity(Intent(context, SettingsActivity::class.java))
                            },
                        )
                    }
                }
            }
        }

        // Copied from setContent {} ext-fun
        setOwners()

        // Register for Login activity results
        component.login.register()

        component.contentViewSetter.setContentView(this, composeView)
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun TiviContent(
    onOpenSettings: () -> Unit,
    analytics: Analytics,
    preferences: TiviPreferences,
) {
    val backstack: SaveableBackStack = rememberSaveableBackStack { push(DiscoverScreen) }
    val circuitNavigator = rememberCircuitNavigator(backstack)

    val navigator: Navigator = remember(circuitNavigator) {
        TiviNavigator(circuitNavigator, onOpenSettings)
    }

    // Launch an effect to track changes to the current back stack entry, and push them
    // as a screen views to analytics
    LaunchedEffect(backstack.topRecord) {
        val topScreen = backstack.topRecord?.screen as? TiviScreen
        analytics.trackScreenView(
            name = topScreen?.name ?: "unknown screen",
            arguments = topScreen?.arguments,
        )
    }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalWindowSizeClass provides calculateWindowSizeClass(),
    ) {
        TiviTheme(
            useDarkColors = preferences.shouldUseDarkColors(),
            useDynamicColors = preferences.shouldUseDynamicColors(),
        ) {
            Home(backstack = backstack, navigator = navigator)
        }
    }
}

private class TiviNavigator(
    private val navigator: Navigator,
    private val onOpenSettings: () -> Unit,
) : Navigator {
    override fun goTo(screen: Screen) {
        when (screen) {
            is SettingsScreen -> onOpenSettings()
            else -> navigator.goTo(screen)
        }
    }

    override fun pop(): Screen? {
        return navigator.pop()
    }

    override fun resetRoot(newRoot: Screen): List<Screen> {
        return navigator.resetRoot(newRoot)
    }
}

@ActivityScope
@Component
abstract class MainActivityComponent(
    @get:Provides override val activity: Activity,
    @Component val applicationComponent: AndroidApplicationComponent = AndroidApplicationComponent.from(activity),
) : ActivityComponent,
    TraktAuthActivityComponent {
    abstract val tiviDateFormatter: TiviDateFormatter
    abstract val textCreator: TiviTextCreator
    abstract val preferences: TiviPreferences
    abstract val analytics: Analytics
    abstract val contentViewSetter: ContentViewSetter
    abstract val login: LoginToTraktInteractor
    abstract val circuitConfig: CircuitConfig
    abstract val imageLoader: ImageLoader
    abstract val viewModel: () -> MainActivityViewModel
}

private fun ComponentActivity.setOwners() {
    val decorView = window.decorView
    if (decorView.findViewTreeLifecycleOwner() == null) {
        decorView.setViewTreeLifecycleOwner(this)
    }
    if (decorView.findViewTreeViewModelStoreOwner() == null) {
        decorView.setViewTreeViewModelStoreOwner(this)
    }
    if (decorView.findViewTreeSavedStateRegistryOwner() == null) {
        decorView.setViewTreeSavedStateRegistryOwner(this)
    }
}
