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

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.data.entities.SortOption
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.domain.PagingInteractor
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObservePagedFollowedShows @Inject constructor(
    private val followedShowsRepository: FollowedShowsRepository,
) : SubjectInteractor<ObservePagedFollowedShows.Parameters, List<FollowedShowsSection>>() {

    override fun createObservable(
        params: Parameters,
    ): Flow<List<FollowedShowsSection>> = flow {
        val result = followedShowsRepository.observeFollowedShows(params.sort, params.filter).map {
            FollowedShowsSection(
                name = it.name,
                source = Pager(config = params.pagingConfig) { it.source }.flow,
            )
        }
        emit(result)
    }

    data class Parameters(
        val filter: String? = null,
        val sort: SortOption,
        override val pagingConfig: PagingConfig,
    ) : PagingInteractor.Parameters<FollowedShowEntryWithShow>
}

data class FollowedShowsSection(
    val name: String,
    val source: Flow<PagingData<FollowedShowEntryWithShow>>,
)