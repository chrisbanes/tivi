// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.showdetails.details

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface ShowDetailsComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindShowDetailsPresenterFactory(factory: ShowDetailsUiPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindShowDetailsUiFactoryFactory(factory: ShowDetailsUiFactory): Ui.Factory = factory
}
