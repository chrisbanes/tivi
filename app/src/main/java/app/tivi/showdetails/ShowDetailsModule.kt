/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.showdetails

import android.content.Context
import app.tivi.AppNavigator
import app.tivi.TiviAppActivityNavigator
import app.tivi.inject.PerActivity
import dagger.Module
import dagger.Provides

@Module
class ShowDetailsModule {
    @Provides
    fun provideShowDetailsNavigator(activity: ShowDetailsActivity): ShowDetailsNavigator {
        return activity.navigatorViewModel
    }

    @Provides
    @PerActivity
    fun provideActivity(activity: ShowDetailsActivity): Context = activity

    @Provides
    fun provideAppNavigator(activity: ShowDetailsActivity): AppNavigator {
        return TiviAppActivityNavigator(activity)
    }
}