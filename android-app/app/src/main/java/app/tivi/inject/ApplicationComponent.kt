// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Application
import android.content.Context
import app.tivi.TiviApplication
import app.tivi.appinitializers.AppInitializers
import app.tivi.common.imageloading.ImageLoadingComponent
import app.tivi.home.ContentViewSetterComponent
import app.tivi.tasks.TasksComponent
import app.tivi.tasks.TiviWorkerFactory
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class ApplicationComponent(
    @get:Provides val application: Application,
) : ApiComponent,
    NetworkComponent,
    AppComponent,
    TasksComponent,
    ImageLoadingComponent,
    CoreComponent,
    DataComponent,
    UiComponent,
    ContentViewSetterComponent,
    VariantAwareComponent {

    abstract val initializers: AppInitializers
    abstract val workerFactory: TiviWorkerFactory

    companion object {
        fun from(context: Context): ApplicationComponent {
            return (context.applicationContext as TiviApplication).component
        }
    }
}
