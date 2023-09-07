// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Activity
import app.tivi.developer.log.DevLogComponent
import app.tivi.home.TiviContent
import app.tivi.settings.developer.DevSettingsComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@ActivityScope
@Component
abstract class AndroidActivityComponent(
    @get:Provides override val activity: Activity,
    @Component val applicationComponent: AndroidApplicationComponent,
) : SharedActivityComponent,
    SharedUiComponent,
    DevSettingsComponent,
    DevLogComponent {
    abstract val tiviContent: TiviContent

    companion object
}
