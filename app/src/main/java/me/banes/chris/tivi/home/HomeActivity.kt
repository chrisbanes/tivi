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
 *
 */

package me.banes.chris.tivi.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import doOnLayout
import kotlinx.android.synthetic.main.activity_home.*
import me.banes.chris.tivi.Constants
import me.banes.chris.tivi.R
import me.banes.chris.tivi.TiviActivity
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.data.TraktUser
import me.banes.chris.tivi.home.HomeActivityViewModel.NavigationItem.*
import me.banes.chris.tivi.home.discover.DiscoverFragment
import me.banes.chris.tivi.home.library.LibraryFragment
import me.banes.chris.tivi.home.trending.PopularShowsFragment
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import updatePadding
import javax.inject.Inject

class HomeActivity : TiviActivity() {

    companion object {
        const val REQUEST_CODE_AUTH = 10
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: HomeActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HomeActivityViewModel::class.java)

        home_bottom_nav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                home_bottom_nav.selectedItemId -> {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        for (i in 0..supportFragmentManager.backStackEntryCount) {
                            supportFragmentManager.popBackStackImmediate()
                        }
                    } else {
                        // TODO scroll to top of main fragment
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

        viewModel.navigationLiveData.observe(this,
                Observer { showNavigationItem(it!!) })

        viewModel.authUiState.observe(this, Observer {
            when (it) {
                HomeActivityViewModel.AuthUiState.LOGGED_IN -> {
                    home_toolbar.menu.findItem(R.id.home_menu_user_avatar).apply {
                        isVisible = true
                        icon = getDrawable(R.drawable.ic_popular)
                    }
                    home_toolbar.menu.findItem(R.id.home_menu_user_login).apply {
                        isVisible = false
                    }
                }
                HomeActivityViewModel.AuthUiState.LOGGED_OUT -> {
                    home_toolbar.menu.findItem(R.id.home_menu_user_avatar).apply {
                        isVisible = false
                    }
                    home_toolbar.menu.findItem(R.id.home_menu_user_login).apply {
                        isVisible = true
                    }
                }
            }
        })

        viewModel.userProfileLifeProfile.observe(this, Observer {
            if (it != null) {
                loadUserProfile(it)
            } else {
                // TODO clear user profile
            }
        })

        home_container_bottom_nav.doOnLayout {
            if (it.height != home_content.paddingBottom) {
                home_content.updatePadding(paddingEnd = it.height)
            }
            true
        }

        home_toolbar.inflateMenu(R.menu.home_toolbar)
        home_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.home_menu_user_avatar -> {
                    // TODO
                    true
                }
                R.id.home_menu_user_login -> {
                    viewModel.startAuthProcess(REQUEST_CODE_AUTH)
                    true
                }
                else -> false
            }
        }

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

        home_appbarlayout.setExpanded(true)

        // Now make the bottom nav show the correct item
        if (home_bottom_nav.selectedItemId != newItemId) {
            home_bottom_nav.menu.findItem(newItemId)?.isChecked = true
        }
    }

    private fun loadUserProfile(user: TraktUser) {
        Glide.with(this)
                .asBitmap()
                .load(user.avatarUrl)
                .into(object : SimpleTarget<Bitmap>(100, 100) {
                    override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                        val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                        circularBitmapDrawable.isCircular = true
                        home_toolbar.menu.findItem(R.id.home_menu_user_avatar).icon = circularBitmapDrawable
                    }
                })
    }

    val navigator = object : HomeNavigator {
        override fun showPopular() {
            supportFragmentManager
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.home_content, PopularShowsFragment())
                    .addToBackStack(null)
                    .commit()
        }

        override fun showTrending() {
            supportFragmentManager
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.home_content, PopularShowsFragment())
                    .addToBackStack(null)
                    .commit()
        }

        override fun showShowDetails(tiviShow: TiviShow) {
            Snackbar.make(home_bottom_nav, "Not implemented yet", Snackbar.LENGTH_SHORT).show()
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
