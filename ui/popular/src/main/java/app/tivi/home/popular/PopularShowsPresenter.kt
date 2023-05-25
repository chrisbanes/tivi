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

package app.tivi.home.popular

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.domain.observers.ObservePagedPopularShows
import app.tivi.screens.PopularShowsScreen
import app.tivi.screens.ShowDetailsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class PopularShowsUiPresenterFactory(
    private val presenterFactory: (Navigator) -> PopularShowsPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? = when (screen) {
        is PopularShowsScreen -> presenterFactory(navigator)
        else -> null
    }
}

@Inject
class PopularShowsPresenter(
    @Assisted private val navigator: Navigator,
    private val pagingInteractor: ObservePagedPopularShows,
) : Presenter<PopularShowsUiState> {

    @Composable
    override fun present(): PopularShowsUiState {
        val items = pagingInteractor.flow.collectAsLazyPagingItems()

        LaunchedEffect(Unit) {
            pagingInteractor(ObservePagedPopularShows.Params(PAGING_CONFIG))
        }

        fun eventSink(event: PopularShowsUiEvent) {
            when (event) {
                PopularShowsUiEvent.NavigateUp -> navigator.pop()
                is PopularShowsUiEvent.OpenShowDetails -> {
                    navigator.goTo(ShowDetailsScreen(event.showId))
                }
            }
        }

        return PopularShowsUiState(
            items = items,
            eventSink = ::eventSink,
        )
    }

    companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 60,
            initialLoadSize = 60,
        )
    }
}
