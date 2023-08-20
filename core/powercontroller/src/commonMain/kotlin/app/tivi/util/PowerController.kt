// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.settings.TiviPreferences
import me.tatarka.inject.annotations.Inject

interface PowerController {
    fun shouldSaveData(): SaveData
}

sealed class SaveData {
    data object Disabled : SaveData()
    data class Enabled(val reason: SaveDataReason) : SaveData()
}

enum class SaveDataReason {
    PREFERENCE, SYSTEM_DATA_SAVER, SYSTEM_POWER_SAVER
}

@Inject
class DefaultPowerController(
    private val preferences: TiviPreferences,
) : PowerController {
    override fun shouldSaveData(): SaveData = when {
        preferences.useLessData -> SaveData.Enabled(SaveDataReason.PREFERENCE)
        else -> SaveData.Disabled
    }
}
