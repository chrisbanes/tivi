// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.daos.LibraryShowsDao
import app.tivi.data.models.SortOption
import app.tivi.domain.PagingInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePagedLibraryShows(
  private val libraryShowsDao: LibraryShowsDao,
) : PagingInteractor<ObservePagedLibraryShows.Parameters, LibraryShow>() {
  @OptIn(androidx.paging.ExperimentalPagingApi::class)
  override fun createObservable(
    params: Parameters,
  ): Flow<PagingData<LibraryShow>> = Pager(config = params.pagingConfig) {
    libraryShowsDao.pagedListLastWatched(
      sort = params.sort,
      filter = if (params.filter.isNullOrEmpty()) null else params.filter,
      onlyFollowed = params.onlyFollowed,
    )
  }.flow

  data class Parameters(
    val sort: SortOption,
    val filter: String? = null,
    val onlyFollowed: Boolean = true,
    override val pagingConfig: PagingConfig,
  ) : PagingInteractor.Parameters<LibraryShow>
}
