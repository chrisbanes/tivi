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

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_home.*
import me.banes.chris.tivi.BuildConfig
import me.banes.chris.tivi.R
import me.banes.chris.tivi.TiviActivity
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.observeK
import me.banes.chris.tivi.home.HomeActivityViewModel.NavigationItem.DISCOVER
import me.banes.chris.tivi.home.HomeActivityViewModel.NavigationItem.LIBRARY
import me.banes.chris.tivi.home.discover.DiscoverFragment
import me.banes.chris.tivi.home.library.LibraryFragment
import me.banes.chris.tivi.home.popular.PopularShowsFragment
import me.banes.chris.tivi.home.trending.TrendingShowsFragment
import me.banes.chris.tivi.home.watched.WatchedShowsFragment
import me.banes.chris.tivi.trakt.TraktConstants
import me.banes.chris.tivi.ui.SharedElementHelper
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject

class HomeActivity : TiviActivity() {

    companion object {
        val ROOT_FRAGMENT = "root"
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeActivityViewModel
    private lateinit var navigatorViewModel: HomeNavigatorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val crashlyticsCore = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        val crashlytics = Crashlytics.Builder().core(crashlyticsCore).build()
        Fabric.with(this, crashlytics)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HomeActivityViewModel::class.java)

        navigatorViewModel = ViewModelProviders.of(this).get(HomeNavigatorViewModel::class.java)

        home_bottom_nav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                home_bottom_nav.selectedItemId -> {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        for (i in 0 until supportFragmentManager.backStackEntryCount) {
                            supportFragmentManager.popBackStackImmediate()
                        }
                    } else {
                        val fragment = supportFragmentManager.findFragmentById(R.id.home_content)
                        when (fragment) {
                            is DiscoverFragment -> fragment.scrollToTop()
                            is LibraryFragment -> fragment.scrollToTop()
                        }
                    }
                    true
                }
                R.id.home_nav_collection -> {
                    viewModel.onNavigationItemClicked(LIBRARY)
                    true
                }
                R.id.home_nav_discover -> {
                    viewModel.onNavigationItemClicked(DISCOVER)
                    true
                }
                else -> false
            }
        }

        viewModel.navigationLiveData.observeK(this, this::showNavigationItem)

        navigatorViewModel.showPopularCall.observeK(this) { this.showPopular() }
        navigatorViewModel.showTrendingCall.observeK(this, this::showTrending)
        navigatorViewModel.showWatchedCall.observeK(this) { this.showWatched() }
        navigatorViewModel.showShowDetailsCall.observeK(this) { it?.let(this::showShowDetails) }
        navigatorViewModel.upClickedCall.observeK(this) { this.onUpClicked() }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun showNavigationItem(item: HomeActivityViewModel.NavigationItem?) {
        if (item == null) {
            return
        }

        val newFragment: Fragment
        val newItemId: Int

        when (item) {
            DISCOVER -> {
                newFragment = DiscoverFragment()
                newItemId = R.id.home_nav_discover
            }
            LIBRARY -> {
                newFragment = LibraryFragment()
                newItemId = R.id.home_nav_collection
            }
        }

        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }
        supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.home_content, newFragment, ROOT_FRAGMENT)
                .commit()

        // Now make the bottom nav show the correct item
        if (home_bottom_nav.selectedItemId != newItemId) {
            home_bottom_nav.menu.findItem(newItemId)?.isChecked = true
        }
    }

    private fun showPopular() {
        supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.home_content, PopularShowsFragment())
                .addToBackStack(null)
                .commit()
    }

    private fun showTrending(sharedElements: SharedElementHelper?) {
        supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.home_content, TrendingShowsFragment())
                .addToBackStack(null)
                .apply {
                    if (sharedElements != null && !sharedElements.isEmpty()) {
                        sharedElements.applyToTransaction(this)
                    } else {
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    }
                }
                .commit()
    }

    private fun showWatched() {
        supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.home_content, WatchedShowsFragment())
                .addToBackStack(null)
                .commit()
    }

    private fun showShowDetails(show: TiviShow) {
        Snackbar.make(home_bottom_nav, "TODO: Open details for ${show.title}", Snackbar.LENGTH_SHORT).show()
    }

    private fun onUpClicked() {
        // TODO can probably do something better here
        supportFragmentManager.popBackStack()
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            TraktConstants.INTENT_ACTION_HANDLE_AUTH_RESPONSE -> {
                val response = AuthorizationResponse.fromIntent(intent)
                val error = AuthorizationException.fromIntent(intent)
                viewModel.onAuthResponse(response, error)
            }
        }
    }
}
