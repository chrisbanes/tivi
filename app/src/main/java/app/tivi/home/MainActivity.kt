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

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import app.tivi.TiviActivity
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.settings.SettingsActivity
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : TiviActivity() {
    private lateinit var viewModel: MainActivityViewModel

    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject internal lateinit var textCreator: TiviTextCreator
    @Inject internal lateinit var preferences: TiviPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        setContent {
            CompositionLocalProvider(
                LocalTiviDateFormatter provides tiviDateFormatter,
                LocalTiviTextCreator provides textCreator,
            ) {
                ProvideWindowInsets(consumeWindowInsets = false) {
                    TiviTheme(useDarkColors = preferences.shouldUseDarkColors()) {
                        Home(onOpenSettings = ::openSettings)
                    }
                }
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}
