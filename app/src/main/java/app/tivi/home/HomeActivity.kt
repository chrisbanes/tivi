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
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import app.tivi.R
import app.tivi.TiviActivityMvRxView
import app.tivi.databinding.ActivityHomeBinding
import app.tivi.extensions.doOnApplyWindowInsets
import app.tivi.extensions.find
import app.tivi.extensions.hideSoftInput
import app.tivi.extensions.updateConstraintSets
import app.tivi.home.main.HomeNavigationEpoxyController
import app.tivi.home.main.HomeNavigationItem
import app.tivi.home.main.homeNavigationItemForDestinationId
import app.tivi.home.search.SearchFragment
import app.tivi.home.search.SearchViewModel
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktConstants
import app.tivi.ui.glide.GlideApp
import app.tivi.ui.glide.asGlideTarget
import app.tivi.ui.navigation.AppBarConfiguration
import app.tivi.ui.navigation.NavigationUI
import app.tivi.ui.navigation.NavigationView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.viewModel
import com.airbnb.mvrx.withState
import com.bumptech.glide.request.target.Target
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import javax.inject.Inject

class HomeActivity : TiviActivityMvRxView() {
    private val authService by lazy(LazyThreadSafetyMode.NONE) { AuthorizationService(this) }

    private val viewModel: HomeActivityViewModel by viewModel()

    private val navigationView = object : NavigationView {
        override fun open() {
            binding.homeRoot.transitionToState(R.id.nav_open)
            // Make sure the keyboard is dismissed when we open the navigation menu
            hideSoftInput()
        }

        override fun close() {
            binding.homeRoot.transitionToState(R.id.nav_closed)
            // Make sure the keyboard is dismissed when we close the navigation menu
            hideSoftInput()
        }

        override fun toggle() {
            binding.homeRoot.run {
                when (currentState) {
                    R.id.nav_closed -> open()
                    else -> close()
                }
            }
        }
    }

    @Inject
    lateinit var homeNavigationViewModelFactory: HomeActivityViewModel.Factory

    private lateinit var binding: ActivityHomeBinding
    private lateinit var userMenuItemGlideTarget: Target<Drawable>

    private val navigationEpoxyController = HomeNavigationEpoxyController(
            object : HomeNavigationEpoxyController.Callbacks {
                override fun onNavigationItemSelected(item: HomeNavigationItem) = showNavigationItem(item)
            })

    private val navController: NavController
        get() = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.homeRoot.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        binding.homeRoot.doOnApplyWindowInsets { v, insets, _ ->
            (v as MotionLayout).updateConstraintSets {
                constrainHeight(R.id.status_scrim, insets.systemWindowInsetTop)
            }
        }

        binding.homeRoot.setTransitionListener(object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout, transitionId: Int) {
                if (transitionId == R.id.nav_closed) {
                    // Clear the search results when the nav is closed
                    binding.homeSearchInput.text = null
                }
            }
        })

        binding.homeToolbar.setOnMenuItemClickListener(::onMenuItemClicked)

        NavigationUI.setupWithNavController(
                binding.homeToolbar,
                navController,
                AppBarConfiguration.Builder(R.id.followed, R.id.watched, R.id.discover)
                        .setNavigationView(navigationView)
                        .build()
        )

        binding.homeSearchInput.addTextChangedListener(object : TextWatcher {
            val searchFragment: SearchFragment = supportFragmentManager.find(R.id.home_search_results)
            val searchViewModel: SearchViewModel by searchFragment.fragmentViewModel()

            override fun afterTextChanged(s: Editable) {
                if (binding.homeRoot.currentState != R.id.nav_closed) {
                    searchViewModel.setSearchQuery(s.toString())

                    if (s.isEmpty()) {
                        binding.homeRoot.transitionToState(R.id.nav_open)
                    } else {
                        binding.homeRoot.transitionToState(R.id.home_constraints_search_results)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Ensure that the keyboard is dismissed when we navigate between fragments
            hideSoftInput()

            // Update our recycler view menu
            navigationEpoxyController.selectedItem = homeNavigationItemForDestinationId(destination.id)
        }

        binding.homeNavRv.setController(navigationEpoxyController)

        userMenuItemGlideTarget = binding.homeToolbar.menu.findItem(R.id.home_menu_user_avatar)
                .asGlideTarget(binding.homeToolbar)
    }

    override fun onStart() {
        super.onStart()
        viewModel.subscribe(this) { postInvalidate() }
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            binding.state = state

            navigationEpoxyController.items = state.navigationItems

            val userMenuItem = binding.homeToolbar.menu.findItem(R.id.home_menu_user_avatar)
            val loginMenuItem = binding.homeToolbar.menu.findItem(R.id.home_menu_user_login)
            if (state.authState == TraktAuthState.LOGGED_IN) {
                userMenuItem.isVisible = true
                state.user?.let { user ->
                    if (user.avatarUrl != null) {
                        GlideApp.with(this)
                                .load(user.avatarUrl)
                                .circleCrop()
                                .into(userMenuItemGlideTarget)
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

    private fun showNavigationItem(item: HomeNavigationItem) {
        fun navigate(id: Int) {
            if (navController.currentDestination?.id != id) {
                navController.navigate(id, null, navOptions {
                    anim {
                        enter = R.anim.nav_default_enter_anim
                        exit = R.anim.nav_default_exit_anim
                        popEnter = R.anim.nav_default_pop_enter_anim
                        popExit = R.anim.nav_default_pop_exit_anim
                    }
                    popUpTo = navController.graph.startDestination
                    launchSingleTop = true
                })
            }
        }
        when (item) {
            HomeNavigationItem.DISCOVER -> navigate(R.id.discover)
            HomeNavigationItem.FOLLOWED -> navigate(R.id.followed)
            HomeNavigationItem.WATCHED -> navigate(R.id.watched)
            HomeNavigationItem.SETTINGS -> {
                navController.navigate(R.id.settings)
                navigationView.close()
            }
        }
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
}
