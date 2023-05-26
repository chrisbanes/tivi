/*
 * Copyright 2017 Google LLC
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

package app.tivi.home

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.shouldUseDynamicColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.core.analytics.Analytics
import app.tivi.data.traktauth.LoginToTraktInteractor
import app.tivi.data.traktauth.TraktAuthActivityComponent
import app.tivi.extensions.unsafeLazy
import app.tivi.inject.ActivityComponent
import app.tivi.inject.ActivityScope
import app.tivi.inject.ApplicationComponent
import app.tivi.screens.DiscoverScreen
import app.tivi.screens.TiviScreen
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.foundation.screen
import com.slack.circuit.runtime.Navigator
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

class MainActivity : TiviActivity() {

    private lateinit var component: MainActivityComponent

    private val viewModel: MainActivityViewModel by viewModels {
        viewModelFactory {
            addInitializer(MainActivityViewModel::class) { component.viewModel() }
        }
    }

    private val preferences: TiviPreferences by unsafeLazy { component.preferences }
    private val analytics: Analytics by unsafeLazy { component.analytics }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component = MainActivityComponent::class.create(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get the viewModel, so it is started and 'running'
        viewModel

        val composeView = ComposeView(this).apply {
            setContent {
                TiviContent()
            }
        }

        // Copied from setContent {} ext-fun
        setOwners()

        // Register for Login activity results
        component.login.register()

        component.contentViewSetter.setContentView(this, composeView)
    }

    @Composable
    private fun TiviContent() {
        CircuitCompositionLocals(component.circuitConfig) {
            val backstack: SaveableBackStack = rememberSaveableBackStack { push(DiscoverScreen) }
            val navigator: Navigator = rememberCircuitNavigator(backstack)

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
                LocalTiviDateFormatter provides component.tiviDateFormatter,
                LocalTiviTextCreator provides component.textCreator,
            ) {
                TiviTheme(
                    useDarkColors = preferences.shouldUseDarkColors(),
                    useDynamicColors = preferences.shouldUseDynamicColors(),
                ) {
                    Home(backstack = backstack, navigator = navigator)
                }
            }
        }
    }
}

@ActivityScope
@Component
abstract class MainActivityComponent(
    @get:Provides val activity: Activity,
    @Component val applicationComponent: ApplicationComponent = ApplicationComponent.from(activity),
) : ActivityComponent,
    TraktAuthActivityComponent {
    abstract val tiviDateFormatter: TiviDateFormatter
    abstract val textCreator: TiviTextCreator
    abstract val preferences: TiviPreferences
    abstract val analytics: Analytics
    abstract val contentViewSetter: ContentViewSetter
    abstract val login: LoginToTraktInteractor
    abstract val circuitConfig: CircuitConfig
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
