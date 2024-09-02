// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.anticipated

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface AnticipatedShowsComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindAnticipatedShowsPresenterFactory(factory: AnticipatedShowsUiPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindAnticipatedShowsUiFactoryFactory(factory: AnticipatedShowsUiFactory): Ui.Factory = factory
}
