/*
 * Copyright 2020 Google LLC
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

@file:Suppress("DEPRECATION")

package app.tivi.settings

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import app.tivi.inject.ApplicationScope
import app.tivi.util.PowerController
import app.tivi.util.PowerControllerModule
import dagger.hilt.android.components.ActivityComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

class SettingsActivity : ComponentActivity() {
    private lateinit var powerController: PowerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // val component = SettingsActivityComponent::class.crea

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
) : ActivityComponent,
    PowerControllerModule,
    SettingsModule {

    abstract val powerController: PowerController
}
