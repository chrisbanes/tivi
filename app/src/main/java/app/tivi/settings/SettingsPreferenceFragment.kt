/*
 * Copyright 2019 Google LLC
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

package app.tivi.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import app.tivi.BuildConfig
import app.tivi.R
import app.tivi.extensions.resolveThemeColor
import app.tivi.util.PowerController
import app.tivi.util.SaveData
import app.tivi.util.SaveDataReason
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect

internal class SettingsPreferenceFragment : PreferenceFragmentCompat(), HasAndroidInjector {
    @Inject lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject lateinit var powerController: PowerController

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            powerController.observeShouldSaveData(ignorePreference = true).collect { saveData ->
                val pref = findPreference<Preference>("pref_data_saver")
                    ?: throw CancellationException()
                val prefDisabled = findPreference<Preference>("pref_data_saver_disabled")
                    ?: throw CancellationException()

                pref.isVisible = saveData is SaveData.Disabled ||
                    (saveData is SaveData.Enabled && saveData.reason == SaveDataReason.PREFERENCE)
                prefDisabled.isVisible = !pref.isVisible
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
            CustomTabsIntent.Builder()
                .setToolbarColor(requireContext().resolveThemeColor(R.attr.colorPrimaryVariant))
                .build()
                .launchUrl(requireContext(), getString(R.string.privacy_policy_url).toUri())
            true
        }

        findPreference<Preference>("open_source")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
            true
        }

        findPreference<Preference>("version")?.apply {
            summary = getString(R.string.settings_app_version_summary,
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        }
    }
}
