// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.opensource

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface OpenSourceComponent {
    @IntoSet
    @Provides
    @ActivityScope
    fun bindOpenSourcePresenterFactory(factory: OpenSourceUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ActivityScope
    fun bindOpenSourceUiFactoryFactory(factory: OpenSourceUiFactory): Ui.Factory = factory
}
