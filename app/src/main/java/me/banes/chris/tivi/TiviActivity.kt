/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import dagger.android.support.DaggerAppCompatActivity
import me.banes.chris.tivi.settings.TiviPreferences
import me.banes.chris.tivi.settings.TiviPreferences.UiTheme.DAY
import me.banes.chris.tivi.settings.TiviPreferences.UiTheme.DAYNIGHT
import me.banes.chris.tivi.settings.TiviPreferences.UiTheme.NIGHT
import javax.inject.Inject

/**
 * Base Activity class which supports LifecycleOwner and Dagger injection.
 */
abstract class TiviActivity : DaggerAppCompatActivity(), LifecycleRegistryOwner {

    private var lifecycleRegistry: LifecycleRegistry? = null
    @Inject protected lateinit var preferences: TiviPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleRegistry = LifecycleRegistry(this)
    }

    override fun onStart() {
        super.onStart()

        when (preferences.uiThemePreference) {
            DAY -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            NIGHT -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            DAYNIGHT -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
        }
    }

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry!!
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry = null
    }

}
