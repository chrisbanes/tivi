// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.discover

import app.tivi.inject.ApplicationScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface DiscoverComponent {
    @IntoSet
    @Provides
    @ApplicationScope
    fun bindDiscoverPresenterFactory(factory: DiscoverUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ApplicationScope
    fun bindDiscoverUiFactoryFactory(factory: DiscoverUiFactory): Ui.Factory = factory
}
