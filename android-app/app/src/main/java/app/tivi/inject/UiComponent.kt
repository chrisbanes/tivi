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

package app.tivi.inject

import app.tivi.account.AccountComponent
import app.tivi.home.discover.DiscoverComponent
import app.tivi.home.library.LibraryComponent
import app.tivi.home.popular.PopularShowsComponent
import app.tivi.home.recommended.RecommendedShowsComponent
import app.tivi.home.search.SearchComponent
import app.tivi.home.trending.TrendingShowsComponent
import app.tivi.showdetails.details.ShowDetailsComponent
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import me.tatarka.inject.annotations.Provides

interface UiComponent :
    AccountComponent,
    DiscoverComponent,
    LibraryComponent,
    PopularShowsComponent,
    RecommendedShowsComponent,
    SearchComponent,
    ShowDetailsComponent,
    TrendingShowsComponent {
    @Provides
    @ApplicationScope
    fun provideCircuitConfig(
        uiFactories: Set<Ui.Factory>,
        presenterFactories: Set<Presenter.Factory>,
    ): CircuitConfig {
        return CircuitConfig.Builder()
            .addUiFactories(uiFactories)
            .addPresenterFactories(presenterFactories)
            .build()
    }
}
