// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.showdetails.seasons

import app.tivi.inject.ApplicationScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface ShowSeasonsComponent {
    @IntoSet
    @Provides
    @ApplicationScope
    fun bindShowSeasonsPresenterFactory(factory: ShowSeasonsUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ApplicationScope
    fun bindShowSeasonsUiFactoryFactory(factory: ShowSeasonsUiFactory): Ui.Factory = factory
}
