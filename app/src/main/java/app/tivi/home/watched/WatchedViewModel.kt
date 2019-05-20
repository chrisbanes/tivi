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

package app.tivi.home.watched

import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.interactors.UpdateWatchedShows
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
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class WatchedViewModel @AssistedInject constructor(
    @Assisted initialState: WatchedViewState,
    private val schedulers: AppRxSchedulers,
    private val updateWatchedShows: UpdateWatchedShows,
    private val traktManager: TraktManager,
    tmdbManager: TmdbManager,
    private val logger: Logger
) : TiviMvRxViewModel<WatchedViewState>(initialState) {
    private val loadingState = RxLoadingCounter()

    private var refreshDisposable: Disposable? = null

    private val filterObservable = BehaviorSubject.create<CharSequence>()

    init {
        loadingState.observable.execute {
            copy(isLoading = it() ?: false)
        }

        tmdbManager.imageProviderObservable
                .delay(50, TimeUnit.MILLISECONDS, schedulers.io)
                .execute { copy(tmdbImageUrlProvider = it() ?: tmdbImageUrlProvider) }

        dataSourceToObservable(updateWatchedShows.dataSourceFactory())
                .execute { copy(watchedShows = it()) }

        filterObservable.distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS, schedulers.main)
                .execute { copy(filter = it() ?: "") }

        refresh()
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
                .subscribe({ refreshWatched() }, logger::e)
                .also { refreshDisposable = it }
    }

    fun setFilter(filter: CharSequence) {
        filterObservable.onNext(filter)
    }

    private fun refreshWatched() {
        loadingState.addLoader()
        viewModelScope.launchInteractor(updateWatchedShows, UpdateWatchedShows.ExecuteParams(false))
                .invokeOnCompletion { loadingState.removeLoader() }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: WatchedViewState): WatchedViewModel
    }

    companion object : MvRxViewModelFactory<WatchedViewModel, WatchedViewState> {
        private val PAGING_CONFIG = PagedList.Config.Builder()
                .setPageSize(60)
                .setPrefetchDistance(20)
                .setEnablePlaceholders(false)
                .build()

        override fun create(viewModelContext: ViewModelContext, state: WatchedViewState): WatchedViewModel? {
            val fragment: WatchedFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.watchedViewModelFactory.create(state)
        }
    }
}
