// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import kotlinx.coroutines.flow.Flow

interface TiviPreferences {

    fun setup()

    var theme: Theme
    fun observeTheme(): Flow<Theme>

    var useDynamicColors: Boolean
    fun observeUseDynamicColors(): Flow<Boolean>

    var useLessData: Boolean
    fun observeUseLessData(): Flow<Boolean>

    var libraryFollowedActive: Boolean
    fun observeLibraryFollowedActive(): Flow<Boolean>

    var libraryWatchedActive: Boolean
    fun observeLibraryWatchedActive(): Flow<Boolean>

    var upNextFollowedOnly: Boolean
    fun observeUpNextFollowedOnly(): Flow<Boolean>

    enum class Theme {
        LIGHT,
        DARK,
        SYSTEM,
    }
}
