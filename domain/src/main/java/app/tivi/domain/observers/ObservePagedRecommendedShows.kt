/*
 * Copyright 2019 Google LLC
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

package app.tivi.domain.observers

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.data.daos.RecommendedDao
import app.tivi.data.resultentities.RecommendedEntryWithShow
import app.tivi.domain.PagingInteractor
import app.tivi.domain.RefreshOnlyRemoteMediator
import app.tivi.domain.interactors.UpdateRecommendedShows
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ObservePagedRecommendedShows @Inject constructor(
    private val RecommendedShowsDao: RecommendedDao,
    private val updateRecommendedShows: UpdateRecommendedShows,
) : PagingInteractor<ObservePagedRecommendedShows.Params, RecommendedEntryWithShow>() {
    override fun createObservable(
        params: Params
    ): Flow<PagingData<RecommendedEntryWithShow>> {
        return Pager(
            config = params.pagingConfig,
            remoteMediator = RefreshOnlyRemoteMediator(GlobalScope) {
                updateRecommendedShows.executeSync(UpdateRecommendedShows.Params())
            },
            pagingSourceFactory = RecommendedShowsDao::entriesPagingSource
        ).flow
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<RecommendedEntryWithShow>
}
