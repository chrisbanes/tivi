// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.home.TiviUiViewController
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIViewController

@ActivityScope
@Component
abstract class HomeUiControllerComponent(
  @Component val applicationComponent: IosApplicationComponent,
) : ProdUiComponent {
  abstract val uiViewControllerFactory: () -> UIViewController

  @Provides
  @ActivityScope
  fun uiViewController(bind: TiviUiViewController): UIViewController = bind()

  companion object
}
