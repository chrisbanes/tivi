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

import android.arch.paging.DataSource
import android.arch.paging.PagedList
import android.arch.paging.RxPagedListBuilder
import android.support.v4.app.FragmentActivity
import app.tivi.SharedElementHelper
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.home.HomeActivity
import app.tivi.home.HomeNavigator
import app.tivi.home.HomeViewModel
import app.tivi.home.library.LibraryFilter.FOLLOWED
import app.tivi.home.library.LibraryFilter.WATCHED
import app.tivi.interactors.SyncFollowedShows
import app.tivi.interactors.UpdateUserDetails
import app.tivi.interactors.UpdateWatchedShows
import app.tivi.tmdb.TmdbManager
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import app.tivi.util.RxLoadingCounter
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.MvRxViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import net.openid.appauth.AuthorizationService
import java.util.concurrent.TimeUnit

class LibraryViewModel @AssistedInject constructor(
    @Assisted initialState: LibraryViewState,
    schedulers: AppRxSchedulers,
    private val updateWatchedShows: UpdateWatchedShows,
    private val syncFollowedShows: SyncFollowedShows,
    private val traktManager: TraktManager,
    tmdbManager: TmdbManager,
    private val updateUserDetails: UpdateUserDetails,
    private val logger: Logger
) : TiviMvRxViewModel<LibraryViewState>(initialState), HomeViewModel {
    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: LibraryViewState): LibraryViewModel
    }

    companion object : MvRxViewModelFactory<LibraryViewState> {
        private val DEFAULT_FILTER = FOLLOWED
        private val PAGING_CONFIG = PagedList.Config.Builder()
                .setPageSize(60)
                .setPrefetchDistance(20)
                .setEnablePlaceholders(false)
                .build()

        @JvmStatic
        override fun create(activity: FragmentActivity, state: LibraryViewState): LibraryViewModel {
            return (activity as HomeActivity).libraryViewModelFactory.create(state)
        }
    }

    private val loadingState = RxLoadingCounter()

    private var refreshDisposable: Disposable? = null

    init {
        setState {
            copy(allowedFilters = LibraryFilter.values().asList(), filter = DEFAULT_FILTER)
        }

        loadingState.observable.execute {
            copy(isLoading = it() ?: false)
        }

        tmdbManager.imageProviderObservable
                .delay(50, TimeUnit.MILLISECONDS, schedulers.io)
                .execute { copy(tmdbImageUrlProvider = it() ?: tmdbImageUrlProvider) }

        dataSourceToObservable(updateWatchedShows.dataSourceFactory())
                .execute {
                    copy(watchedShows = it())
                }

        dataSourceToObservable(syncFollowedShows.dataSourceFactory())
                .execute {
                    copy(followedShows = it())
                }

        updateUserDetails.setParams(UpdateUserDetails.Params("me"))
        updateUserDetails.observe()
                .toObservable()
                .execute { copy(user = it()) }

        traktManager.state.distinctUntilChanged()
                .doOnNext {
                    if (it == TraktAuthState.LOGGED_IN) {
                        launchInteractor(updateUserDetails, UpdateUserDetails.ExecuteParams(false))
                    }
                }
                .execute { copy(authState = it() ?: TraktAuthState.LOGGED_OUT) }
    }

    private fun <T : EntryWithShow<*>> dataSourceToObservable(f: DataSource.Factory<Int, T>): Observable<PagedList<T>> {
        return RxPagedListBuilder(f, PAGING_CONFIG).setBoundaryCallback(object : PagedList.BoundaryCallback<T>() {
            override fun onZeroItemsLoaded() {
                setState { copy(isEmpty = true) }
            }

            override fun onItemAtEndLoaded(itemAtEnd: T) {
                setState { copy(isEmpty = false) }
            }

            override fun onItemAtFrontLoaded(itemAtFront: T) {
                setState { copy(isEmpty = false) }
            }
        }).buildObservable()
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
        withState {
            when (it.filter) {
                FOLLOWED -> {
                    loadingState.addLoader()
                    launchInteractor(syncFollowedShows, SyncFollowedShows.ExecuteParams(false))
                            .invokeOnCompletion {
                                loadingState.removeLoader()
                            }
                }
                WATCHED -> {
                    loadingState.addLoader()
                    launchInteractor(updateWatchedShows, UpdateWatchedShows.ExecuteParams(false))
                            .invokeOnCompletion {
                                loadingState.removeLoader()
                            }
                }
            }
        }
    }

    fun onFilterSelected(filter: LibraryFilter) {
        setState {
            copy(filter = filter)
        }
    }

    fun onItemPostedClicked(navigator: HomeNavigator, show: TiviShow, sharedElements: SharedElementHelper? = null) {
        navigator.showShowDetails(show, sharedElements)
    }

    override fun onProfileItemClicked() {
        // TODO
    }

    override fun onLoginItemClicked(authService: AuthorizationService) {
        traktManager.startAuth(0, authService)
    }
}
