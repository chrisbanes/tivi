// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.upnext

import app.tivi.inject.ApplicationScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface UpNextComponent {
    @IntoSet
    @Provides
    @ApplicationScope
    fun bindUpNextPresenterFactory(factory: UpNextUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ApplicationScope
    fun bindUpNextUiFactoryFactory(factory: UpNextUiFactory): Ui.Factory = factory
}
