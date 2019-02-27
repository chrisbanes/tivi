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

package app.tivi.home.library

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.commit
import app.tivi.R
import app.tivi.databinding.FragmentLibraryBinding
import app.tivi.extensions.updateConstraintSets
import app.tivi.home.HomeActivity
import app.tivi.home.HomeNavigator
import app.tivi.home.library.followed.FollowedFragment
import app.tivi.home.library.watched.WatchedFragment
import app.tivi.trakt.TraktAuthState
import app.tivi.ui.glide.GlideApp
import app.tivi.ui.glide.asGlideTarget
import app.tivi.util.GridToGridTransitioner
import app.tivi.util.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.bumptech.glide.request.target.Target
import javax.inject.Inject

class LibraryFragment : TiviMvRxFragment() {
    @Inject lateinit var homeNavigator: HomeNavigator
    private lateinit var binding: FragmentLibraryBinding

    private val viewModel: LibraryViewModel by fragmentViewModel()
    @Inject lateinit var libraryViewModelFactory: LibraryViewModel.Factory

    private lateinit var userMenuItemGlideTarget: Target<Drawable>

    private val filterController = LibraryFiltersEpoxyController(object : LibraryFiltersEpoxyController.Callbacks {
        override fun onFilterSelected(filter: LibraryFilter) {
            closeFilterPanel()
            viewModel.onFilterSelected(filter)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GridToGridTransitioner.setupFirstFragment(this, R.id.summary_appbarlayout, R.id.summary_status_scrim)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        view.setOnApplyWindowInsetsListener { _, insets ->
            binding.libraryMotion.updateConstraintSets {
                it.constrainHeight(R.id.summary_status_scrim, insets.systemWindowInsetTop)
            }
            // Just return insets
            insets
        }
        // Finally, request some insets
        view.requestApplyInsets()

        binding.libraryFiltersRv.adapter = filterController.adapter

        binding.libraryToolbar.run {
            inflateMenu(R.menu.home_toolbar)
            setOnMenuItemClickListener(this@LibraryFragment::onMenuItemClicked)
        }

        userMenuItemGlideTarget = binding.libraryToolbar.menu.findItem(R.id.home_menu_user_avatar)
                .asGlideTarget(binding.libraryToolbar)
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (binding.state == null) {
                // First time we've had state, start any postponed transitions
                scheduleStartPostponedTransitions()
            }

            binding.state = state
            filterController.setData(state)

            when (state.filter) {
                LibraryFilter.WATCHED -> {
                    val fragment = childFragmentManager.findFragmentById(R.id.library_content_fragment)
                    if (fragment !is WatchedFragment) {
                        childFragmentManager.commit {
                            replace(R.id.library_content_fragment, WatchedFragment())
                        }
                    }
                }
                LibraryFilter.FOLLOWED -> {
                    val fragment = childFragmentManager.findFragmentById(R.id.library_content_fragment)
                    if (fragment !is FollowedFragment) {
                        childFragmentManager.commit {
                            replace(R.id.library_content_fragment, FollowedFragment())
                        }
                    }
                }
            }

            val userMenuItem = binding.libraryToolbar.menu.findItem(R.id.home_menu_user_avatar)
            val loginMenuItem = binding.libraryToolbar.menu.findItem(R.id.home_menu_user_login)
            when (state.authState) {
                TraktAuthState.LOGGED_IN -> {
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
                }
                TraktAuthState.LOGGED_OUT -> {
                    userMenuItem.isVisible = false
                    loginMenuItem.isVisible = true
                }
            }

            // Close the filter pane if needed
            closeFilterPanel()
        }
    }

    internal fun scrollToTop() {
        closeFilterPanel()

        when (val f = childFragmentManager.findFragmentById(R.id.library_content)) {
            is WatchedFragment -> f.scrollToTop()
            is FollowedFragment -> f.scrollToTop()
        }
    }

    private fun closeFilterPanel() {
        binding.libraryMotion.transitionToStart()
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
            viewModel.onSettingsClicked(homeNavigator)
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
