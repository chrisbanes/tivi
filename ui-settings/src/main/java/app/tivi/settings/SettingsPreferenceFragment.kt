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

@file:Suppress("DEPRECATION")

package app.tivi.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import app.tivi.extensions.resolveThemeColor
import app.tivi.util.SaveData
import app.tivi.util.SaveDataReason

internal class SettingsPreferenceFragment : PreferenceFragment() {
    internal var saveData: SaveData? = null
        set(value) {
            val pref = findPreference("pref_data_saver") as? SwitchPreference
                ?: throw IllegalStateException()

            pref.isEnabled = when (value) {
                is SaveData.Enabled -> value.reason == SaveDataReason.PREFERENCE
                else -> true
            }

            if (pref.isEnabled) {
                pref.summary = null
                pref.setSummaryOn(R.string.settings_data_saver_summary_on)
            } else {
                pref.summaryOn = null
                pref.setSummary(R.string.settings_data_saver_summary_system)
            }

            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)

        findPreference("privacy_policy")?.setOnPreferenceClickListener {
            CustomTabsIntent.Builder()
                .setToolbarColor(context.resolveThemeColor(android.R.attr.colorPrimary))
                .build()
                .launchUrl(context, getString(R.string.privacy_policy_url).toUri())
            true
        }

        findPreference("version")?.apply {
            val pkgManager: PackageManager = context.packageManager
            val pkgInfo = pkgManager.getPackageInfo(context.packageName, 0)
            summary = getString(
                R.string.settings_app_version_summary,
                pkgInfo.versionName,
                PackageInfoCompat.getLongVersionCode(pkgInfo)
            )
        }
    }
}
