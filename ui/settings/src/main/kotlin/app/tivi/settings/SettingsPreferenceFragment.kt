// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package app.tivi.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import androidx.annotation.AttrRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.content.res.use
import androidx.core.net.toUri
import app.tivi.common.ui.resources.Locales
import app.tivi.common.ui.resources.Strings
import app.tivi.util.SaveData
import app.tivi.util.SaveDataReason
import cafe.adriel.lyricist.Lyricist

internal class SettingsPreferenceFragment : PreferenceFragment() {

    val lyricist = Lyricist(Locales.EN, Strings)

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
                pref.summaryOn = lyricist.strings.settingsDataSaverSummaryOn
            } else {
                pref.summaryOn = null
                pref.summary = lyricist.strings.settingsDataSaverSummarySystem
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
                .launchUrl(context, lyricist.strings.privacyPolicyUrl.toUri())
            true
        }

        findPreference("version")?.apply {
            val pkgManager: PackageManager = context.packageManager
            val pkgInfo = pkgManager.getPackageInfo(context.packageName, 0)
            summary = lyricist.strings.settingsAppVersionSummary(
                pkgInfo.versionName,
                PackageInfoCompat.getLongVersionCode(pkgInfo).toInt(),
            )
        }

        if (Build.VERSION.SDK_INT < 31) {
            val category = findPreference("pref_category_ui") as PreferenceCategory?
            category?.removePreference(findPreference("pref_dynamic_colors"))
        }
    }
}

@SuppressLint("Recycle")
private fun Context.resolveThemeColor(@AttrRes resId: Int, defaultColor: Int = Color.MAGENTA): Int {
    return obtainStyledAttributes(intArrayOf(resId)).use {
        it.getColor(0, defaultColor)
    }
}
