// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.popular

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface PopularShowsComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindPopularShowsPresenterFactory(factory: PopularShowsUiPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindPopularShowsUiFactoryFactory(factory: PopularShowsUiFactory): Ui.Factory = factory
}
