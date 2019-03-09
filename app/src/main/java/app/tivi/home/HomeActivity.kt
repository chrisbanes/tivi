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
import android.view.MenuItem
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import app.tivi.R
import app.tivi.TiviActivity
import app.tivi.databinding.FragmentHomeBinding
import app.tivi.extensions.updateConstraintSets
import app.tivi.home.main.HomeNavigationEpoxyController
import app.tivi.home.main.HomeNavigationItem
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktConstants
import app.tivi.ui.glide.GlideApp
import app.tivi.ui.glide.asGlideTarget
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.viewModel
import com.airbnb.mvrx.withState
import com.bumptech.glide.request.target.Target
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import javax.inject.Inject

class HomeActivity : TiviActivity(), MvRxView {

    private val authService by lazy(LazyThreadSafetyMode.NONE) { AuthorizationService(this) }

    private val viewModel: HomeActivityViewModel by viewModel()

    @Inject lateinit var homeNavigationViewModelFactory: HomeActivityViewModel.Factory

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userMenuItemGlideTarget: Target<Drawable>

    private val controller = HomeNavigationEpoxyController(object : HomeNavigationEpoxyController.Callbacks {
        override fun onNavigationItemSelected(item: HomeNavigationItem) = showNavigationItem(item)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.fragment_home)

        binding.root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        binding.root.setOnApplyWindowInsetsListener { _, insets ->
            binding.homeRoot.updateConstraintSets {
                it.constrainHeight(R.id.status_scrim, insets.systemWindowInsetTop)
            }
            // Just return insets
            insets
        }
        // Finally, request some insets
        binding.root.requestApplyInsets()

        binding.homeToolbar.apply {
            inflateMenu(R.menu.discover_toolbar)
            setOnMenuItemClickListener(::onMenuItemClicked)
        }

        binding.homeNavRv.setController(controller)

        userMenuItemGlideTarget = binding.homeToolbar.menu.findItem(R.id.home_menu_user_avatar)
                .asGlideTarget(binding.homeToolbar)

        showNavigationItem(HomeNavigationItem.DISCOVER)
    }

    override fun onStart() {
        super.onStart()
        postInvalidate()
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            binding.state = state

            controller.setData(state)

            showNavigationItem(state.currentNavigationItem)

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
        when (item) {
            HomeNavigationItem.DISCOVER -> {
                getNavController().navigate(R.id.discover)
            }
            HomeNavigationItem.FOLLOWED -> {
                getNavController().navigate(R.id.followed)
            }
            HomeNavigationItem.WATCHED -> {
                getNavController().navigate(R.id.watched)
            }
        }
        // Close the menu if we've changed fragments
        binding.homeRoot.transitionToStart()
    }

    private fun getNavController(): NavController {
        return (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
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
        R.id.home_settings -> {
            getNavController().navigate(R.id.settings)
            true
        }
        R.id.home_privacy_policy -> {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(
                    this,
                    "https://chrisbanes.github.io/tivi/privacypolicy.html".toUri()
            )
            true
        }
        else -> false
    }
}
