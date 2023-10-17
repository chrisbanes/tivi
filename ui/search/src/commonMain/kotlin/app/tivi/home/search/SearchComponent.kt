// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.search

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface SearchComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindSearchPresenterFactory(factory: SearchUiPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindSearchUiFactoryFactory(factory: SearchUiFactory): Ui.Factory = factory
}
