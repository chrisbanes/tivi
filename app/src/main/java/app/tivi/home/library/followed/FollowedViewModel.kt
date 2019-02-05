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

package app.tivi.home.library.followed

import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import app.tivi.SharedElementHelper
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.home.HomeNavigator
import app.tivi.home.library.LibraryViewModel
import app.tivi.home.library.LibraryViewState
import app.tivi.interactors.SyncFollowedShows
import app.tivi.interactors.launchInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import app.tivi.util.RxLoadingCounter
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import java.util.concurrent.TimeUnit

class FollowedViewModel @AssistedInject constructor(
    @Assisted initialState: FollowedViewState,
    private val schedulers: AppRxSchedulers,
    private val syncFollowedShows: SyncFollowedShows,
    private val traktManager: TraktManager,
    tmdbManager: TmdbManager,
    private val logger: Logger
) : TiviMvRxViewModel<FollowedViewState>(initialState) {
    private val loadingState = RxLoadingCounter()

    private var refreshDisposable: Disposable? = null

    init {
        loadingState.observable.execute {
            copy(isLoading = it() ?: false)
        }

        tmdbManager.imageProviderObservable
                .delay(50, TimeUnit.MILLISECONDS, schedulers.io)
                .execute { copy(tmdbImageUrlProvider = it() ?: tmdbImageUrlProvider) }

        dataSourceToObservable(syncFollowedShows.dataSourceFactory())
                .execute {
                    copy(followedShows = it())
                }
    }

    private fun <T : EntryWithShow<*>> dataSourceToObservable(f: DataSource.Factory<Int, T>): Observable<PagedList<T>> {
        return RxPagedListBuilder(f, PAGING_CONFIG)
                .setBoundaryCallback(object : PagedList.BoundaryCallback<T>() {
                    override fun onZeroItemsLoaded() = setState { copy(isEmpty = true) }
                    override fun onItemAtEndLoaded(itemAtEnd: T) = setState { copy(isEmpty = false) }
                    override fun onItemAtFrontLoaded(itemAtFront: T) = setState { copy(isEmpty = false) }
                })
                .setFetchScheduler(schedulers.io)
                .setNotifyScheduler(schedulers.main)
                .buildObservable()
    }

    fun refresh() {
        refreshDisposable?.let {
            it.dispose()
            disposables.remove(it)
        }
        refreshDisposable = null

        disposables += traktManager.state
                .filter { it == TraktAuthState.LOGGED_IN }
                .firstOrError()
                .subscribe({ refreshFilter() }, logger::e)
                .also { refreshDisposable = it }
    }

    private fun refreshFilter() {
        loadingState.addLoader()
        scope.launchInteractor(syncFollowedShows, SyncFollowedShows.ExecuteParams(false))
                .invokeOnCompletion {
                    loadingState.removeLoader()
                }
    }

    fun onItemPostedClicked(navigator: HomeNavigator, show: TiviShow, sharedElements: SharedElementHelper? = null) {
        navigator.showShowDetails(show, sharedElements)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: LibraryViewState): LibraryViewModel
    }

    companion object : MvRxViewModelFactory<LibraryViewModel, LibraryViewState> {
        private val PAGING_CONFIG = PagedList.Config.Builder()
                .setPageSize(60)
                .setPrefetchDistance(20)
                .setEnablePlaceholders(false)
                .build()

        override fun create(viewModelContext: ViewModelContext, state: LibraryViewState): LibraryViewModel? {
            val fragment: FollowedFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.followedViewModelFactory.create(state)
        }
    }
}
