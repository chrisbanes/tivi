/*
 * Copyright 2022 Google LLC
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
import app.tivi.data.daos.LibraryShowsDao
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.LibraryShow
import app.tivi.domain.PagingInteractor
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePagedLibraryShows @Inject constructor(
    private val libraryShowsDao: LibraryShowsDao,
) : PagingInteractor<ObservePagedLibraryShows.Parameters, LibraryShow>() {

    override fun createObservable(
        params: Parameters,
    ): Flow<PagingData<LibraryShow>> = Pager(config = params.pagingConfig) {
        libraryShowsDao.observeForPaging(params.sort, params.filter)
    }.flow

    data class Parameters(
        val filter: String? = null,
        val sort: SortOption,
        override val pagingConfig: PagingConfig,
    ) : PagingInteractor.Parameters<LibraryShow>
}
