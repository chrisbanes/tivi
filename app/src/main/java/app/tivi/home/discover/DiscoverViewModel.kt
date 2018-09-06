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

package app.tivi.home.discover

import android.support.v4.app.FragmentActivity
import app.tivi.SharedElementHelper
import app.tivi.data.entities.TiviShow
import app.tivi.home.HomeActivity
import app.tivi.home.HomeNavigator
import app.tivi.home.HomeViewModel
import app.tivi.interactors.SearchShows
import app.tivi.interactors.UpdatePopularShows
import app.tivi.interactors.UpdateTrendingShows
import app.tivi.interactors.UpdateUserDetails
import app.tivi.tmdb.TmdbImageUrlProvider
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
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import net.openid.appauth.AuthorizationService
import java.util.concurrent.TimeUnit

class DiscoverViewModel @AssistedInject constructor(
    @Assisted initialState: DiscoverViewState,
    schedulers: AppRxSchedulers,
    private val updatePopularShows: UpdatePopularShows,
    private val updateTrendingShows: UpdateTrendingShows,
    private val searchShows: SearchShows,
    private val traktManager: TraktManager,
    tmdbManager: TmdbManager,
    private val updateUserDetails: UpdateUserDetails,
    logger: Logger
) : TiviMvRxViewModel<DiscoverViewState>(initialState), HomeViewModel {
    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: DiscoverViewState): DiscoverViewModel
    }

    companion object : MvRxViewModelFactory<DiscoverViewState> {
        @JvmStatic
        override fun create(activity: FragmentActivity, state: DiscoverViewState): DiscoverViewModel {
            return (activity as HomeActivity).discoverViewModelFactory.create(state)
        }
    }

    private var searchQuery = BehaviorSubject.create<String>()
    private val loadingState = RxLoadingCounter()

    init {
        disposables += searchQuery.observeOn(schedulers.main)
                .debounce(500, TimeUnit.MILLISECONDS, schedulers.io)
                .distinctUntilChanged()
                .subscribe(::runSearchQuery, logger::e)

        searchShows.observe()
                .toObservable()
                .execute { copy(searchResults = it()) }

        tmdbManager.imageProviderObservable
                .delay(50, TimeUnit.MILLISECONDS, schedulers.io)
                .execute { copy(tmdbImageUrlProvider = it() ?: TmdbImageUrlProvider()) }

        loadingState.observable
                .execute { copy(isLoading = it() ?: false) }

        updateTrendingShows.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute { copy(trendingItems = it() ?: emptyList()) }

        updatePopularShows.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute { copy(popularItems = it() ?: emptyList()) }

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

        refresh()
    }

    fun refresh() {
        loadingState.addLoader()
        launchInteractor(updatePopularShows, UpdatePopularShows.ExecuteParams(UpdatePopularShows.Page.REFRESH))
                .invokeOnCompletion { loadingState.removeLoader() }

        loadingState.addLoader()
        launchInteractor(updateTrendingShows, UpdateTrendingShows.ExecuteParams(UpdateTrendingShows.Page.REFRESH))
                .invokeOnCompletion { loadingState.removeLoader() }
    }

    fun onTrendingHeaderClicked(navigator: HomeNavigator, sharedElementHelper: SharedElementHelper? = null) {
        navigator.showTrending(sharedElementHelper)
    }

    fun onPopularHeaderClicked(navigator: HomeNavigator, sharedElementHelper: SharedElementHelper? = null) {
        navigator.showPopular(sharedElementHelper)
    }

    fun onItemPosterClicked(navigator: HomeNavigator, show: TiviShow, sharedElements: SharedElementHelper?) {
        navigator.showShowDetails(show, sharedElements)
    }

    private fun runSearchQuery(query: String) {
        launchInteractor(searchShows, SearchShows.Params(query))
    }

    fun onSearchOpened() {
        setState {
            copy(isSearchOpen = true)
        }
    }

    fun onSearchClosed() {
        setState {
            copy(isSearchOpen = false)
        }
    }

    fun onSearchQueryChanged(query: String) = searchQuery.onNext(query)

    override fun onProfileItemClicked() {
        // TODO
    }

    override fun onLoginItemClicked(authService: AuthorizationService) {
        traktManager.startAuth(0, authService)
    }
}
