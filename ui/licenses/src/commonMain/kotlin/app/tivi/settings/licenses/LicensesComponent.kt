// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.licenses

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface LicensesComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindLicensesPresenterFactory(factory: LicensesUiPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindLicensesUiFactoryFactory(factory: LicensesUiFactory): Ui.Factory = factory
}
