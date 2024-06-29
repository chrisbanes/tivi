// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.developer.notifications

import app.tivi.inject.ActivityScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface DevNotificationsComponent {
  @IntoSet
  @Provides
  @ActivityScope
  fun bindDevNotificationsPresenterFactory(factory: DevNotificationsPresenterFactory): Presenter.Factory = factory

  @IntoSet
  @Provides
  @ActivityScope
  fun bindDevNotificationsUiFactoryFactory(factory: DevNotificationsUiFactory): Ui.Factory = factory
}
