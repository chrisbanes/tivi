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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProviders
import app.tivi.R
import app.tivi.data.Entry
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.databinding.FragmentDiscoverBinding
import app.tivi.extensions.setActionViewExpanded
import app.tivi.home.HomeActivity
import app.tivi.home.HomeNavigator
import app.tivi.home.HomeNavigatorViewModel
import app.tivi.trakt.TraktAuthState
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.glide.GlideApp
import app.tivi.ui.glide.asGlideTarget
import app.tivi.util.GridToGridTransitioner
import app.tivi.util.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState

internal class DiscoverFragment : TiviMvRxFragment() {
    private lateinit var binding: FragmentDiscoverBinding
    private lateinit var searchView: SearchView
    private lateinit var listItemSharedElementHelper: ListItemSharedElementHelper

    private lateinit var homeNavigator: HomeNavigator

    private val viewModel: DiscoverViewModel by fragmentViewModel()

    private val controller = DiscoverEpoxyController(object : DiscoverEpoxyController.Callbacks {
        override fun onTrendingHeaderClicked(items: List<TrendingEntryWithShow>) {
            viewModel.onTrendingHeaderClicked(homeNavigator,
                    listItemSharedElementHelper.createForItems(items))
        }

        override fun onPopularHeaderClicked(items: List<PopularEntryWithShow>) {
            viewModel.onPopularHeaderClicked(homeNavigator,
                    listItemSharedElementHelper.createForItems(items))
        }

        override fun onItemClicked(viewHolderId: Long, item: EntryWithShow<out Entry>) {
            viewModel.onItemPosterClicked(homeNavigator, item.show,
                    listItemSharedElementHelper.createForId(viewHolderId, "poster"))
        }

        override fun onSearchItemClicked(viewHolderId: Long, item: TiviShow) {
            viewModel.onItemPosterClicked(homeNavigator, item,
                    listItemSharedElementHelper.createForId(viewHolderId, "poster"))
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeNavigator = ViewModelProviders.of(activity!!, viewModelFactory).get(HomeNavigatorViewModel::class.java)

        GridToGridTransitioner.setupFirstFragment(this,
                intArrayOf(R.id.summary_appbarlayout, R.id.summary_status_scrim))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.summaryRv.apply {
            setController(controller)
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        listItemSharedElementHelper = ListItemSharedElementHelper(binding.summaryRv)

        binding.summaryToolbar.apply {
            inflateMenu(R.menu.discover_toolbar)
            setOnMenuItemClickListener(this@DiscoverFragment::onMenuItemClicked)

            val searchItem = menu.findItem(R.id.discover_search)
            searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    viewModel.onSearchOpened()
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    viewModel.onSearchClosed()
                    return true
                }
            })

            searchView = menu.findItem(R.id.discover_search).actionView as SearchView
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.onSearchQueryChanged(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                viewModel.onSearchQueryChanged(query)
                return true
            }
        })

        binding.summarySwipeRefresh.setOnRefreshListener(viewModel::refresh)
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            binding.state = state
            controller.setData(state)

            val searchMenuItem = binding.summaryToolbar.menu.findItem(R.id.discover_search)
            searchMenuItem.setActionViewExpanded(state.isSearchOpen)

            val userMenuItem = binding.summaryToolbar.menu.findItem(R.id.home_menu_user_avatar)
            val loginMenuItem = binding.summaryToolbar.menu.findItem(R.id.home_menu_user_login)
            when (state.authState) {
                TraktAuthState.LOGGED_IN -> {
                    userMenuItem.isVisible = true
                    state.user?.let { user ->
                        userMenuItem.title = user.name
                        if (user.avatarUrl != null) {
                            GlideApp.with(requireContext())
                                    .load(user.avatarUrl)
                                    .circleCrop()
                                    .into(userMenuItem.asGlideTarget(binding.summaryToolbar))
                        }
                    }
                    loginMenuItem.isVisible = false
                }
                TraktAuthState.LOGGED_OUT -> {
                    userMenuItem.isVisible = false
                    loginMenuItem.isVisible = true
                }
            }

            scheduleStartPostponedTransitions()
        }
    }

    internal fun scrollToTop() {
        binding.summaryRv.apply {
            stopScroll()
            smoothScrollToPosition(0)
        }
        binding.summaryAppbarlayout.setExpanded(true)
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
