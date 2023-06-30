// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.home.TiviUiViewController
import me.tatarka.inject.annotations.Component
import platform.UIKit.UIViewController

@ActivityScope
@Component
abstract class HomeUiControllerComponent(
    @Component val applicationComponent: IosApplicationComponent,
) : UiComponent {
    abstract val viewController: TiviUiViewController

    /**
     * Function which makes [viewController] easier to call from Swift
     */
    fun uiViewController(
        onRootPop: () -> Unit,
        onOpenSettings: () -> Unit,
    ): UIViewController = viewController(onRootPop, onOpenSettings)

    companion object
}
