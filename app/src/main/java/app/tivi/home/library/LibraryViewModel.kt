/*
 * Copyright 2017 Google, Inc.
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

package app.tivi.home.library

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import android.arch.paging.PagedList
import android.arch.paging.RxPagedListBuilder
import app.tivi.SharedElementHelper
import app.tivi.data.entities.TiviShow
import app.tivi.datasources.trakt.FollowedShowsDataSource
import app.tivi.datasources.trakt.WatchedShowsDataSource
import app.tivi.extensions.toFlowable
import app.tivi.home.HomeFragmentViewModel
import app.tivi.home.HomeNavigator
import app.tivi.home.library.LibraryFilter.FOLLOWED
import app.tivi.home.library.LibraryFilter.WATCHED
import app.tivi.interactors.FetchWatchedShowsInteractor
import app.tivi.interactors.SyncAllFollowedShowsInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import app.tivi.util.NetworkDetector
import app.tivi.util.RxLoadingCounter
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    private val schedulers: AppRxSchedulers,
    private val watchedShowsDataSource: WatchedShowsDataSource,
    private val watchedShowsInteractor: FetchWatchedShowsInteractor,
    private val followedDataSource: FollowedShowsDataSource,
    private val followedShowsInteractor: SyncAllFollowedShowsInteractor,
    private val traktManager: TraktManager,
    private val tmdbManager: TmdbManager,
    private val networkDetector: NetworkDetector,
    logger: Logger
) : HomeFragmentViewModel(traktManager, logger) {
    private val _data = MutableLiveData<LibraryViewState>()
    val data: LiveData<LibraryViewState>
        get() = _data

    private val loadingState = RxLoadingCounter()

    private val currentFilter = BehaviorSubject.create<LibraryFilter>()

    private var refreshDisposable: Disposable? = null

    private val pagingConfig by lazy(LazyThreadSafetyMode.NONE) {
        PagedList.Config.Builder()
                .setPageSize(60)
                .setPrefetchDistance(20)
                .setEnablePlaceholders(false)
                .build()
    }

    init {
        disposables += currentFilter.toFlowable()
                .distinctUntilChanged()
                .flatMap(::createFilterViewStateFlowable)
                .observeOn(schedulers.main)
                .subscribe(_data::setValue, logger::e)

        disposables += currentFilter.distinctUntilChanged()
                .observeOn(schedulers.main)
                .subscribe({ refresh() }, logger::e)

        // Trigger the default filter
        currentFilter.onNext(FOLLOWED)
    }

    private fun createFilterViewStateFlowable(filter: LibraryFilter): Flowable<LibraryViewState> = when (filter) {
        WATCHED -> {
            Flowables.combineLatest(
                    Flowable.just(LibraryFilter.values().asList()),
                    Flowable.just(filter),
                    tmdbManager.imageProvider,
                    loadingState.observable.toFlowable(),
                    dataSourceToFlowable(watchedShowsDataSource.dataSourceFactory()),
                    ::LibraryWatchedViewState)
        }
        FOLLOWED -> {
            Flowables.combineLatest(
                    Flowable.just(LibraryFilter.values().asList()),
                    Flowable.just(filter),
                    tmdbManager.imageProvider,
                    loadingState.observable.toFlowable(),
                    dataSourceToFlowable(followedDataSource.dataSourceFactory()),
                    ::LibraryFollowedViewState)
        }
    }

    private fun <T> dataSourceToFlowable(source: DataSource.Factory<Int, T>): Flowable<PagedList<T>> {
        return RxPagedListBuilder(source, pagingConfig)
                .setFetchScheduler(schedulers.io)
                .setNotifyScheduler(schedulers.main)
                .setBoundaryCallback(object : PagedList.BoundaryCallback<T>() {
                })
                .buildFlowable(BackpressureStrategy.LATEST)
    }

    fun refresh() {
        if (refreshDisposable != null) {
            refreshDisposable?.dispose()
            refreshDisposable = null
        }

        disposables += Observables.combineLatest(
                networkDetector.waitForConnection().toObservable(),
                traktManager.state.filter { it == TraktAuthState.LOGGED_IN })
                .firstOrError()
                .subscribe({ refreshFilter() }, logger::e)
                .also { refreshDisposable = it }
    }

    private fun refreshFilter() {
        when (currentFilter.value) {
            FOLLOWED -> {
                loadingState.addLoader()
                launchInteractor(followedShowsInteractor).invokeOnCompletion {
                    loadingState.removeLoader()
                }
            }
            WATCHED -> {
                loadingState.addLoader()
                launchInteractor(watchedShowsInteractor).invokeOnCompletion {
                    loadingState.removeLoader()
                }
            }
        }
    }

    fun onWatchedHeaderClicked(navigator: HomeNavigator, sharedElements: SharedElementHelper) {
        navigator.showWatched(sharedElements)
    }

    fun onMyShowsHeaderClicked(navigator: HomeNavigator, sharedElements: SharedElementHelper) {
        navigator.showMyShows(sharedElements)
    }

    fun onFilterSelected(filter: LibraryFilter) {
        currentFilter.onNext(filter)
    }

    fun onItemPostedClicked(navigator: HomeNavigator, show: TiviShow, sharedElements: SharedElementHelper? = null) {
        navigator.showShowDetails(show, sharedElements)
    }
}
