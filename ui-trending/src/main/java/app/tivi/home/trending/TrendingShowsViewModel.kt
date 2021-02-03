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

import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.domain.observers.ObservePagedTrendingShows
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class TrendingShowsViewModel @Inject constructor(
    private val pagingInteractor: ObservePagedTrendingShows,
) : ViewModel() {

    val pagedList: Flow<PagingData<TrendingEntryWithShow>>
        get() = pagingInteractor.observe()

    init {
        pagingInteractor(ObservePagedTrendingShows.Params(PAGING_CONFIG))
    }

    companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 60,
            initialLoadSize = 60
        )
    }
}
