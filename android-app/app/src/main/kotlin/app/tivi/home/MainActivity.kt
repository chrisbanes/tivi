// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.view.WindowCompat
import app.tivi.TiviActivity
import app.tivi.inject.ActivityComponent
import app.tivi.inject.ActivityScope
import app.tivi.inject.AndroidApplicationComponent
import app.tivi.inject.UiComponent
import app.tivi.screens.DiscoverScreen
import app.tivi.settings.SettingsActivity
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

class MainActivity : TiviActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = MainActivityComponent::class.create(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val backstack = rememberSaveableBackStack { push(DiscoverScreen) }
            val navigator = rememberCircuitNavigator(backstack)

            component.tiviContent(
                backstack = backstack,
                navigator = navigator,
                onOpenSettings = {
                    val context = this@MainActivity
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                },
                modifier = Modifier.semantics {
                    // Enables testTag -> UiAutomator resource id
                    // See https://developer.android.com/jetpack/compose/testing#uiautomator-interop
                    @OptIn(ExperimentalComposeUiApi::class)
                    testTagsAsResourceId = true
                },
            )
        }
    }
}

@ActivityScope
@Component
abstract class MainActivityComponent(
    @get:Provides override val activity: Activity,
    @Component val applicationComponent: AndroidApplicationComponent = AndroidApplicationComponent.from(activity),
) : ActivityComponent, UiComponent {
    abstract val tiviContent: TiviContent
}
