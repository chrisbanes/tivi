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

package me.banes.chris.tivi.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import me.banes.chris.tivi.calls.TmdbShowFetcher
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.plusAssign
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.RxAwareViewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import timber.log.Timber
import javax.inject.Inject

internal class HomeActivityViewModel @Inject constructor(
        private val traktManager: TraktManager,
        private val tiviShowDao: TiviShowDao,
        private val tmdbShowFetcher: TmdbShowFetcher,
        private val schedulers: AppRxSchedulers
) : RxAwareViewModel() {

    enum class NavigationItem {
        DISCOVER, LIBRARY
    }

    private val mutableNavLiveData = MutableLiveData<NavigationItem>()

    /**
     * Facade so that we don't leak the fact that its mutable
     */
    val navigationLiveData: LiveData<NavigationItem>
        get() = mutableNavLiveData

    init {
        // Set default value
        mutableNavLiveData.value = NavigationItem.DISCOVER

        setupTiviShowTmdbUpdater()
    }

    fun onNavigationItemClicked(item: NavigationItem) {
        mutableNavLiveData.value = item
    }

    fun onAuthResponse(response: AuthorizationResponse?, ex: AuthorizationException?) {
        when {
            ex != null -> traktManager.onAuthException(ex)
            response != null -> traktManager.onAuthResponse(response)
        }
    }

    /**
     * This shouldn't really live here, but its a convenient place for now
     */
    private fun setupTiviShowTmdbUpdater() {
        disposables += tiviShowDao.getShowsWhichNeedTmdbUpdate()
                .subscribeOn(schedulers.database)
                .subscribe({
                    it.filter(tmdbShowFetcher::startUpdate).forEach(this::refreshShowFromTmdb)
                }, {
                    Timber.e(it, "Error while refreshing shows from TVDb")
                })
    }

    private fun refreshShowFromTmdb(show: TiviShow) {
        Timber.d("Updating show from TMDb: %s", show)
        disposables += tmdbShowFetcher.updateShow(show.tmdbId!!)
                .subscribe({
                    Timber.d("Updated show from TMDb %s", show)
                }, {
                    Timber.e(it, "Error while refreshing show from TMDb", show)
                })
    }
}
