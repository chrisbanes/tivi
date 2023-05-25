/*
 * Copyright 2023 Google LLC
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

package app.tivi.home.trending

import app.tivi.inject.ApplicationScope
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface TrendingShowsComponent {
    @IntoSet
    @Provides
    @ApplicationScope
    fun bindTrendingShowsPresenterFactory(factory: TrendingShowsUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ApplicationScope
    fun bindTrendingShowsUiFactoryFactory(factory: TrendingShowsUiFactory): Ui.Factory = factory
}
