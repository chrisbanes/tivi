// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.upnext

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface UpNextComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindUpNextPresenterFactory(factory: UpNextUiPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindUpNextUiFactoryFactory(factory: UpNextUiFactory): Ui.Factory = factory
}
