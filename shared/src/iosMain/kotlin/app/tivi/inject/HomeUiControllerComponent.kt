// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.data.traktauth.LoginToTraktInteractor
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.home.TiviUiViewController
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIViewController

@ActivityScope
@Component
abstract class HomeUiControllerComponent(
    @Component val applicationComponent: IosApplicationComponent,
    private val loginToTraktInteractorProvider: (TraktOAuthInfo, () -> UIViewController) -> LoginToTraktInteractor,
) : UiComponent {
    abstract val uiViewControllerFactory: () -> UIViewController

    @Provides
    @ActivityScope
    fun uiViewController(bind: TiviUiViewController): UIViewController = bind()

    @Provides
    @ActivityScope
    fun provideLoginToTraktInteractor(info: TraktOAuthInfo): LoginToTraktInteractor {
        return loginToTraktInteractorProvider(info, uiViewControllerFactory)
    }

    companion object
}
