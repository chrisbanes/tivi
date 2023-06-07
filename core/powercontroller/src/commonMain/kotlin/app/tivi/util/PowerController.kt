// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface PowerController {
    fun observeShouldSaveData(ignorePreference: Boolean): Flow<SaveData>
    fun shouldSaveData(): SaveData
}

sealed class SaveData {
    object Disabled : SaveData()
    data class Enabled(val reason: SaveDataReason) : SaveData()
}

object EmptyPowerController: PowerController {
    override fun observeShouldSaveData(ignorePreference: Boolean): Flow<SaveData> = emptyFlow()

    override fun shouldSaveData(): SaveData = SaveData.Disabled
}

enum class SaveDataReason {
    PREFERENCE, SYSTEM_DATA_SAVER, SYSTEM_POWER_SAVER
}
