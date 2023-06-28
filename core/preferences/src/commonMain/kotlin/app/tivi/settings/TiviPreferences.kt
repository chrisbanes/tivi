// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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

object EmptyTiviPreferences : TiviPreferences {
    override fun setup() = Unit

    override var theme: TiviPreferences.Theme = TiviPreferences.Theme.SYSTEM

    override fun observeTheme(): Flow<TiviPreferences.Theme> = emptyFlow()

    override var useDynamicColors: Boolean = false

    override fun observeUseDynamicColors(): Flow<Boolean> = emptyFlow()
    override var useLessData: Boolean = false

    override fun observeUseLessData(): Flow<Boolean> = emptyFlow()

    override var libraryFollowedActive: Boolean = true

    override fun observeLibraryFollowedActive(): Flow<Boolean> = emptyFlow()

    override var libraryWatchedActive: Boolean = true

    override fun observeLibraryWatchedActive(): Flow<Boolean> = emptyFlow()

    override var upNextFollowedOnly: Boolean = true

    override fun observeUpNextFollowedOnly(): Flow<Boolean> = emptyFlow()
}
