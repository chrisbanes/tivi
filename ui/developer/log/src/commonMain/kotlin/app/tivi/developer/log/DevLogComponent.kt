// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.developer.log

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface DevLogComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindDevLogPresenterFactory(factory: DevLogUiPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindDevLogUiFactoryFactory(factory: DevLogUiFactory): Ui.Factory = factory
}
