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
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.tivi.ContentViewSetter
import app.tivi.TiviActivity
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.shouldUseDynamicColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.inject.ActivityComponent
import app.tivi.inject.ApplicationComponent
import app.tivi.settings.SettingsActivity
import app.tivi.settings.TiviPreferences
import app.tivi.util.Analytics
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@AndroidEntryPoint
class MainActivity : TiviActivity() {

    // private val component = MainActivityComponent::c

    private lateinit var viewModel: MainActivityViewModel

    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter

    @Inject internal lateinit var textCreator: TiviTextCreator

    @Inject internal lateinit var preferences: TiviPreferences

    @Inject internal lateinit var analytics: Analytics

    @Inject internal lateinit var contentViewSetter: ContentViewSetter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        val composeView = ComposeView(this).apply {
            setContent {
                TiviContent()
            }
        }

        // Copied from setContent {} ext-fun
        setOwners()

        contentViewSetter.setContentView(this, composeView)
    }

    @Composable
    private fun TiviContent() {
        CompositionLocalProvider(
            LocalTiviDateFormatter provides tiviDateFormatter,
            LocalTiviTextCreator provides textCreator,
        ) {
            TiviTheme(
                useDarkColors = preferences.shouldUseDarkColors(),
                useDynamicColors = preferences.shouldUseDynamicColors(),
            ) {
                Home(
                    analytics = analytics,
                    onOpenSettings = {
                        startActivity(
                            Intent(this@MainActivity, SettingsActivity::class.java),
                        )
                    },
                )
            }
        }
    }
}

@Component
abstract class MainActivityComponent(
    @get:Provides override val activity: Activity,
    @Component val applicationComponent: ApplicationComponent = ApplicationComponent.from(activity),
) : ActivityComponent {
    abstract val tiviDateFormatter: TiviDateFormatter
    abstract val textCreator: TiviTextCreator
    abstract val preferences: TiviPreferences
    abstract val analytics: Analytics
    abstract val contentViewSetter: ContentViewSetter
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
