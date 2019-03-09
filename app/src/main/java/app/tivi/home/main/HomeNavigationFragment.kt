/*
 * Copyright 2019 Google LLC
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

package app.tivi.home.main

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import app.tivi.R
import app.tivi.SharedElementHelper
import app.tivi.databinding.FragmentHomeBinding
import app.tivi.extensions.observeK
import app.tivi.extensions.updateConstraintSets
import app.tivi.home.HomeActivity
import app.tivi.home.HomeNavigatorViewModel
import app.tivi.home.discover.DiscoverFragment
import app.tivi.home.library.followed.FollowedFragment
import app.tivi.home.library.watched.WatchedFragment
import app.tivi.home.popular.PopularShowsFragment
import app.tivi.home.trending.TrendingShowsFragment
import app.tivi.trakt.TraktAuthState
import app.tivi.ui.glide.GlideApp
import app.tivi.ui.glide.asGlideTarget
import app.tivi.util.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.bumptech.glide.request.target.Target
import javax.inject.Inject

class HomeNavigationFragment : TiviMvRxFragment() {

    companion object {
        const val ROOT_FRAGMENT = "root"
    }

    private val viewModel: HomeNavigationViewModel by fragmentViewModel()
    private lateinit var homeNavigatorViewModel: HomeNavigatorViewModel

    @Inject lateinit var homeNavigationViewModelFactory: HomeNavigationViewModel.Factory
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userMenuItemGlideTarget: Target<Drawable>

    private val controller = HomeNavigationEpoxyController(object : HomeNavigationEpoxyController.Callbacks {
        override fun onNavigationItemSelected(item: HomeNavigationItem) {
            viewModel.onNavigationItemSelected(item)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeNavigatorViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory)
                .get(HomeNavigatorViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        view.setOnApplyWindowInsetsListener { _, insets ->
            binding.homeRoot.updateConstraintSets {
                it.constrainHeight(R.id.status_scrim, insets.systemWindowInsetTop)
            }
            // Just return insets
            insets
        }
        // Finally, request some insets
        view.requestApplyInsets()

        binding.homeToolbar.apply {
            inflateMenu(R.menu.discover_toolbar)
            setOnMenuItemClickListener(::onMenuItemClicked)
        }

        binding.homeNavRv.setController(controller)

        userMenuItemGlideTarget = binding.homeToolbar.menu.findItem(R.id.home_menu_user_avatar)
                .asGlideTarget(binding.homeToolbar)

        homeNavigatorViewModel.showPopularCall.observeK(this) {
            showStackFragment(PopularShowsFragment(), it)
        }
        homeNavigatorViewModel.showTrendingCall.observeK(this) {
            showStackFragment(TrendingShowsFragment(), it)
        }
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            controller.setData(state)

            showNavigationItem(state.currentNavigationItem)
            binding.homeToolbarTitle.setText(state.currentNavigationItem.labelResId)

            val userMenuItem = binding.homeToolbar.menu.findItem(R.id.home_menu_user_avatar)
            val loginMenuItem = binding.homeToolbar.menu.findItem(R.id.home_menu_user_login)
            if (state.authState == TraktAuthState.LOGGED_IN) {
                userMenuItem.isVisible = true
                state.user?.let { user ->
                    if (user.avatarUrl != null) {
                        GlideApp.with(requireContext())
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

    private fun showNavigationItem(item: HomeNavigationItem) {
        val newFragment: Fragment = when (item) {
            HomeNavigationItem.DISCOVER -> DiscoverFragment()
            HomeNavigationItem.FOLLOWED -> FollowedFragment()
            HomeNavigationItem.WATCHED -> WatchedFragment()
        }

        val currentFragment = childFragmentManager.findFragmentById(R.id.home_content)

        if (currentFragment == null || currentFragment::class != newFragment::class) {
            childFragmentManager.popBackStackImmediate(ROOT_FRAGMENT, 0)

            childFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.home_content, newFragment, ROOT_FRAGMENT)
                    .commit()

            // Close the menu if we've changed fragments
            binding.homeRoot.transitionToStart()
        }
    }

    private fun showStackFragment(fragment: Fragment, sharedElements: SharedElementHelper? = null) {
        childFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.home_content, fragment)
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

    private fun onMenuItemClicked(item: MenuItem) = when (item.itemId) {
        R.id.home_menu_user_avatar -> {
            viewModel.onProfileItemClicked()
            true
        }
        R.id.home_menu_user_login -> {
            viewModel.onLoginItemClicked((requireActivity() as HomeActivity).authService)
            true
        }
        R.id.home_settings -> {
            viewModel.onSettingsClicked(homeNavigatorViewModel)
            true
        }
        R.id.home_privacy_policy -> {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(
                    requireContext(),
                    "https://chrisbanes.github.io/tivi/privacypolicy.html".toUri()
            )
            true
        }
        else -> false
    }
}
