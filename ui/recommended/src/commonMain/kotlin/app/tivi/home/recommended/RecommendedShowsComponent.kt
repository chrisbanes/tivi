// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.recommended

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface RecommendedShowsComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindRecommendedShowsPresenterFactory(factory: RecommendedShowsUiPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindRecommendedShowsUiFactoryFactory(factory: RecommendedShowsUiFactory): Ui.Factory = factory
}
