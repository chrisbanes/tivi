// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package app.tivi.settings

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import app.tivi.inject.ApplicationScope
import app.tivi.util.PowerController
import app.tivi.util.PowerControllerComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

class SettingsActivity : ComponentActivity() {
    private lateinit var powerController: PowerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = SettingsActivityComponent::class.create(application)
        powerController = component.powerController

        val fragment = SettingsPreferenceFragment()

        fragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .commit()

        lifecycleScope.launchWhenStarted {
            powerController.observeShouldSaveData(ignorePreference = true).collect { saveData ->
                fragment.saveData = saveData
            }
        }
    }
}

@Component
@ApplicationScope
abstract class SettingsActivityComponent(
    @get:Provides val application: Application,
) : PowerControllerComponent, PreferencesComponent {
    abstract val powerController: PowerController
}
