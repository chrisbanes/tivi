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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_home.*
import me.banes.chris.tivi.Constants
import me.banes.chris.tivi.R
import me.banes.chris.tivi.TiviActivity
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.home.HomeActivityViewModel.NavigationItem.DISCOVER
import me.banes.chris.tivi.home.HomeActivityViewModel.NavigationItem.LIBRARY
import me.banes.chris.tivi.home.discover.DiscoverFragment
import me.banes.chris.tivi.home.library.LibraryFragment
import me.banes.chris.tivi.home.popular.PopularShowsFragment
import me.banes.chris.tivi.home.trending.TrendingShowsFragment
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import me.banes.chris.tivi.BuildConfig

class HomeActivity : TiviActivity() {

    companion object {
        const val REQUEST_CODE_AUTH = 10
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val crashlyticsCore = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        val crashlytics = Crashlytics.Builder().core(crashlyticsCore).build()
        Fabric.with(this, crashlytics)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HomeActivityViewModel::class.java)

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

        viewModel.navigationLiveData.observe(this, Observer {
            showNavigationItem(it!!)
        })

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun showNavigationItem(item: HomeActivityViewModel.NavigationItem) {
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

        supportFragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.home_content, newFragment)
                .commit()

        // Now make the bottom nav show the correct item
        if (home_bottom_nav.selectedItemId != newItemId) {
            home_bottom_nav.menu.findItem(newItemId)?.isChecked = true
        }
    }

    val navigator = object : HomeNavigator {
        override fun showPopular() {
            supportFragmentManager
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.home_content, PopularShowsFragment())
                    .addToBackStack(null)
                    .commit()
        }

        override fun showTrending() {
            supportFragmentManager
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.home_content, TrendingShowsFragment())
                    .addToBackStack(null)
                    .commit()
        }

        override fun showShowDetails(tiviShow: TiviShow) {
            Snackbar.make(home_bottom_nav, "TODO: Open show details", Snackbar.LENGTH_SHORT).show()
        }

        override fun onUpClicked() {
            // TODO can probably do something better here
            supportFragmentManager.popBackStack()
        }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Constants.INTENT_ACTION_HANDLE_AUTH_RESPONSE -> {
                val response = AuthorizationResponse.fromIntent(intent)
                val error = AuthorizationException.fromIntent(intent)
                viewModel.onAuthResponse(response, error)
            }
        }
    }
}
