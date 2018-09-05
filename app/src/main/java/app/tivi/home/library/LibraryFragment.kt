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

package app.tivi.home.library

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import app.tivi.R
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.databinding.FragmentLibraryBinding
import app.tivi.home.HomeActivity
import app.tivi.home.HomeNavigator
import app.tivi.home.HomeNavigatorViewModel
import app.tivi.trakt.TraktAuthState
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.epoxy.EmptyEpoxyController
import app.tivi.ui.glide.GlideApp
import app.tivi.ui.glide.asGlideTarget
import app.tivi.util.GridToGridTransitioner
import app.tivi.util.TiviMvRxFragment
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState

class LibraryFragment : TiviMvRxFragment() {

    private lateinit var homeNavigator: HomeNavigator
    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var binding: FragmentLibraryBinding

    private val viewModel: LibraryViewModel by fragmentViewModel()

    private val listItemSharedElementHelper by lazy(LazyThreadSafetyMode.NONE) {
        ListItemSharedElementHelper(binding.libraryRv) { it.findViewById(R.id.show_poster) }
    }

    private var controller: EpoxyController = EmptyEpoxyController
        set(value) {
            if (field != value) {
                field = value
                binding.libraryRv.adapter = value.adapter
                value.spanCount = gridLayoutManager.spanCount
                gridLayoutManager.spanSizeLookup = controller.spanSizeLookup
            }
        }

    private val filterController = LibraryFiltersEpoxyController(object : LibraryFiltersEpoxyController.Callbacks {
        override fun onFilterSelected(filter: LibraryFilter) {
            closeFilterPanel()
            viewModel.onFilterSelected(filter)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeNavigator = ViewModelProviders.of(activity!!, viewModelFactory).get(HomeNavigatorViewModel::class.java)

        GridToGridTransitioner.setupFirstFragment(this,
                intArrayOf(R.id.summary_appbarlayout, R.id.summary_status_scrim))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        binding.setLifecycleOwner(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        // Setup span and columns
        gridLayoutManager = binding.libraryRv.layoutManager as GridLayoutManager

        binding.libraryRv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        binding.libraryFiltersRv.adapter = filterController.adapter

        binding.libraryToolbar.run {
            inflateMenu(R.menu.home_toolbar)
            setOnMenuItemClickListener(this@LibraryFragment::onMenuItemClicked)
        }

        binding.librarySwipeRefresh.setOnRefreshListener(viewModel::refresh)

        binding.libraryToolbarTitle.setOnClickListener {
            // TODO this should look at direction
            val motion = binding.libraryMotion
            if (motion.progress > 0.5f) {
                motion.transitionToStart()
            } else {
                motion.transitionToEnd()
            }
        }
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            binding.state = state
            filterController.setData(state)

            when (state.filter) {
                LibraryFilter.WATCHED -> {
                    val c = (controller as? LibraryWatchedEpoxyController ?: createWatchedController())
                    c.tmdbImageUrlProvider = state.tmdbImageUrlProvider
                    c.setList(state.watchedShows)
                    c.isEmpty = state.isEmpty
                    controller = c
                }
                LibraryFilter.FOLLOWED -> {
                    val c = controller as? LibraryFollowedEpoxyController ?: createFollowedController()
                    c.tmdbImageUrlProvider = state.tmdbImageUrlProvider
                    c.setList(state.followedShows)
                    c.isEmpty = state.isEmpty
                    controller = c
                }
            }

            val userMenuItem = binding.libraryToolbar.menu.findItem(R.id.home_menu_user_avatar)
            val loginMenuItem = binding.libraryToolbar.menu.findItem(R.id.home_menu_user_login)
            when (state.authState) {
                TraktAuthState.LOGGED_IN -> {
                    userMenuItem.isVisible = true
                    state.user?.let { user ->
                        userMenuItem.title = user.name
                        if (user.avatarUrl != null) {
                            GlideApp.with(requireContext())
                                    .load(user.avatarUrl)
                                    .circleCrop()
                                    .into(userMenuItem.asGlideTarget())
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
            scheduleStartPostponedTransitions()
        }
    }

    internal fun scrollToTop() {
        binding.libraryRv.stopScroll()
        binding.libraryRv.smoothScrollToPosition(0)
    }

    private fun closeFilterPanel() {
        binding.libraryMotion.transitionToStart()
    }

    private fun createWatchedController() = LibraryWatchedEpoxyController(
            object : LibraryWatchedEpoxyController.Callbacks {
                override fun onItemClicked(item: WatchedShowEntryWithShow) {
                    viewModel.onItemPostedClicked(homeNavigator, item.show,
                            listItemSharedElementHelper.createForItem(item, "poster")
                    )
                }
            })

    private fun createFollowedController() = LibraryFollowedEpoxyController(
            object : LibraryFollowedEpoxyController.Callbacks {
                override fun onItemClicked(item: FollowedShowEntryWithShow) {
                    viewModel.onItemPostedClicked(homeNavigator, item.show,
                            listItemSharedElementHelper.createForItem(item, "poster")
                    )
                }
            })

    private fun onMenuItemClicked(item: MenuItem) = when (item.itemId) {
        R.id.home_menu_user_avatar -> {
            viewModel.onProfileItemClicked()
            true
        }
        R.id.home_menu_user_login -> {
            viewModel.onLoginItemClicked((requireActivity() as HomeActivity).authService)
            true
        }
        else -> false
    }
}
