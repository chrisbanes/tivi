// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import app.tivi.core.analytics.Analytics
import app.tivi.data.traktauth.LoginToTraktInteractor
import app.tivi.data.traktauth.TraktAuthActivityComponent
import app.tivi.inject.ActivityComponent
import app.tivi.inject.ActivityScope
import app.tivi.inject.AndroidApplicationComponent
import app.tivi.settings.SettingsActivity
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitConfig
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

class MainActivity : TiviActivity() {

    private lateinit var component: MainActivityComponent

    private val viewModel: MainActivityViewModel by viewModels {
        viewModelFactory {
            addInitializer(MainActivityViewModel::class) { component.viewModel() }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
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
                            onRootPop = {
                                if (onBackPressedDispatcher.hasEnabledCallbacks()) {
                                    onBackPressedDispatcher.onBackPressed()
                                }
                            },
                            onOpenSettings = {
                                context.startActivity(Intent(context, SettingsActivity::class.java))
                            },
                            modifier = Modifier.semantics {
                                // Enables testTag -> UiAutomator resource id
                                // See https://developer.android.com/jetpack/compose/testing#uiautomator-interop
                                testTagsAsResourceId = true
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
