// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.library

import app.tivi.inject.ApplicationScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface LibraryComponent {
    @IntoSet
    @Provides
    @ApplicationScope
    fun bindLibraryPresenterFactory(factory: LibraryUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ApplicationScope
    fun bindLibraryUiFactoryFactory(factory: LibraryUiFactory): Ui.Factory = factory
}
