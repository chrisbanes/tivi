/*
 * Copyright 2017 Google LLC
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

package app.tivi.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import app.tivi.R
import app.tivi.TiviActivityMvRxView
import app.tivi.databinding.ActivityHomeBinding
import app.tivi.extensions.beginDelayedTransition
import app.tivi.extensions.doOnApplyWindowInsets
import app.tivi.extensions.hideSoftInput
import app.tivi.home.main.HomeNavigationItemDiffAdapter
import app.tivi.home.main.HomeNavigationItemDiffCallback
import app.tivi.home.search.SearchFragment
import app.tivi.home.search.SearchViewModel
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktConstants
import app.tivi.util.AppCoroutineDispatchers
import coil.Coil
import coil.api.load
import coil.size.PixelSize
import coil.size.Size
import coil.size.SizeResolver
import coil.transform.CircleCropTransformation
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.viewModel
import com.airbnb.mvrx.withState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import javax.inject.Inject

class HomeActivity : TiviActivityMvRxView() {
    private val authService by lazy(LazyThreadSafetyMode.NONE) { AuthorizationService(this) }

    private val viewModel: HomeActivityViewModel by viewModel()

    @Inject
    lateinit var homeNavigationViewModelFactory: HomeActivityViewModel.Factory

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    private lateinit var binding: ActivityHomeBinding

    private val navHostFragment: NavHostFragment
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

    private val navController: NavController
        get() = navHostFragment.navController

    private val primaryNavigationFragment: Fragment?
        get() = navHostFragment.childFragmentManager.primaryNavigationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.homeRoot.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        binding.statusScrim.doOnApplyWindowInsets { view, insets, _ ->
            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = insets.systemWindowInsetTop
                validate()
            }
        }

        binding.homeToolbar.setOnMenuItemClickListener(::onMenuItemClicked)

        val searchMenuItem = binding.homeToolbar.menu.findItem(R.id.home_menu_search)
        searchMenuItem.setOnActionExpandListener(SearchViewListeners())

        binding.homeToolbar.setupWithNavController(
                navController,
                AppBarConfiguration.Builder(
                        R.id.navigation_followed,
                        R.id.navigation_watched,
                        R.id.navigation_discover,
                        R.id.navigation_settings
                ).build()
        )

        binding.homeBottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            // Ensure that the keyboard is dismissed when we navigate between fragments
            hideSoftInput()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.subscribe(this) { postInvalidate() }
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            binding.state = state

            val bottomNavMenu = binding.homeBottomNavigation.menu
            val diffCallback = HomeNavigationItemDiffCallback(
                    state.navigationItems, bottomNavMenu)
            val result = DiffUtil.calculateDiff(diffCallback, false)
            result.dispatchUpdatesTo(
                    HomeNavigationItemDiffAdapter(state.navigationItems, bottomNavMenu))

            val userMenuItem = binding.homeToolbar.menu.findItem(R.id.home_menu_user_avatar)
            val loginMenuItem = binding.homeToolbar.menu.findItem(R.id.home_menu_user_login)
            if (state.authState == TraktAuthState.LOGGED_IN) {
                userMenuItem.isVisible = true
                if (state.user?.avatarUrl != null) {
                    Coil.load(this, state.user.avatarUrl) {
                        transformations(CircleCropTransformation())
                        size(object : SizeResolver {
                            override suspend fun size(): Size {
                                val height = binding.homeToolbar.height
                                return PixelSize(height, height)
                            }
                        })
                        target { userMenuItem.icon = it }
                    }
                }
                loginMenuItem.isVisible = false
            } else if (state.authState == TraktAuthState.LOGGED_OUT) {
                userMenuItem.isVisible = false
                loginMenuItem.isVisible = true
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent.action) {
            TraktConstants.INTENT_ACTION_HANDLE_AUTH_RESPONSE -> {
                val response = AuthorizationResponse.fromIntent(intent)
                val error = AuthorizationException.fromIntent(intent)
                viewModel.onAuthResponse(authService, response, error)
            }
        }
    }

    override fun onBackPressed() {
        if (binding.homeToolbar.hasExpandedActionView()) {
            binding.homeToolbar.collapseActionView()
            return
        }
        super.onBackPressed()
    }

    private fun onMenuItemClicked(item: MenuItem) = when (item.itemId) {
        R.id.home_menu_user_avatar -> {
            viewModel.onProfileItemClicked()
            true
        }
        R.id.home_menu_user_login -> {
            viewModel.onLoginItemClicked(authService)
            true
        }
        else -> false
    }

    private inner class SearchViewListeners : SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
        private var expandedMenuItem: MenuItem? = null

        override fun onQueryTextSubmit(query: String): Boolean {
            (primaryNavigationFragment as? SearchFragment)?.run {
                val searchViewModel: SearchViewModel by fragmentViewModel()
                searchViewModel.setSearchQuery(query)
            }
            hideSoftInput()
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            (primaryNavigationFragment as? SearchFragment)?.run {
                val searchViewModel: SearchViewModel by fragmentViewModel()
                searchViewModel.setSearchQuery(newText)
            }
            return true
        }

        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
            expandedMenuItem = item

            val searchView = item.actionView as SearchView
            searchView.setOnQueryTextListener(this)

            binding.homeToolbar.beginDelayedTransition(100)

            // Open the search fragment
            navController.navigate(R.id.navigation_search)

            return true
        }

        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
            binding.homeToolbar.beginDelayedTransition(100)

            expandedMenuItem = null

            val searchView = item.actionView as SearchView
            searchView.setOnQueryTextListener(null)

            (primaryNavigationFragment as? SearchFragment)?.run {
                val searchViewModel: SearchViewModel by fragmentViewModel()
                searchViewModel.clearQuery()
            }

            // Pop the search fragment off
            if (navController.currentDestination?.id == R.id.navigation_search) {
                navController.popBackStack()
            }

            return true
        }
    }
}
