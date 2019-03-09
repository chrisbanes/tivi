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

package app.tivi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.tivi.settings.TiviPreferences
import app.tivi.settings.TiviPreferences.DarkMode
import com.airbnb.mvrx.BaseMvRxActivity
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * Base Activity class which supports LifecycleOwner and Dagger injection.
 */
abstract class TiviActivity : BaseMvRxActivity(), HasSupportFragmentInjector {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var prefs: TiviPreferences

    @Inject lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        updateNightMode()
    }

    private var postponedTransition = false

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        updateNightMode()
    }

    override fun postponeEnterTransition() {
        super.postponeEnterTransition()
        postponedTransition = true
    }

    override fun startPostponedEnterTransition() {
        postponedTransition = false
        super.startPostponedEnterTransition()
    }

    fun scheduleStartPostponedTransitions() {
        if (postponedTransition) {
            window.decorView.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    open fun handleIntent(intent: Intent) {}

    override fun finishAfterTransition() {
        val resultData = Intent()
        val result = onPopulateResultIntent(resultData)
        setResult(result, resultData)

        super.finishAfterTransition()
    }

    open fun onPopulateResultIntent(intent: Intent): Int {
        return Activity.RESULT_OK
    }

    private fun updateNightMode() {
        when (prefs.darkModePreference) {
            DarkMode.SYSTEM -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            DarkMode.ALWAYS -> delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> updateNightModeForBatterySaver()
        }
    }

    private fun updateNightModeForBatterySaver() {
        val darkMode = prefs.darkModePreference

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isPowerSaveMode) {
            // Since we're in battery saver mode,
            if (darkMode == DarkMode.BATTERY_SAVER_ONLY || darkMode == DarkMode.AUTO) {
                delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        } else {
            // If we're not in power saving mode, we can just use the default states
            if (darkMode == DarkMode.AUTO) {
                delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
            } else if (darkMode == DarkMode.BATTERY_SAVER_ONLY) {
                delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment>? {
        return supportFragmentInjector
    }
}
