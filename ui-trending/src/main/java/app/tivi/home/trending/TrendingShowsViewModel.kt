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

package app.tivi.home.trending

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.ReduxViewModel
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.domain.observers.ObservePagedTrendingShows
import app.tivi.util.ObservableLoadingCounter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class TrendingShowsViewModel @ViewModelInject constructor(
    private val pagingInteractor: ObservePagedTrendingShows,
) : ReduxViewModel<TrendingViewState>(TrendingViewState()) {

    val pagedList: Flow<PagingData<TrendingEntryWithShow>>
        get() = pagingInteractor.observe()

    private val pendingActions = Channel<TrendingAction>(Channel.BUFFERED)

    private val loadingState = ObservableLoadingCounter()

    init {
        viewModelScope.launch {
            loadingState.observable
                .distinctUntilChanged()
                .debounce(2000)
                .collectAndSetState { copy(isLoading = it) }
        }

        pagingInteractor(ObservePagedTrendingShows.Params(PAGING_CONFIG))

//        viewModelScope.launch {
//            pendingActions.consumeAsFlow().collect { action ->
//                // TODO
//            }
//        }
    }

    fun submitAction(action: TrendingAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) pendingActions.send(action)
        }
    }

    companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 60,
            initialLoadSize = 60
        )
    }
}
