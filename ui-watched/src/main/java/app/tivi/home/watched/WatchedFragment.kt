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

package app.tivi.home.watched

import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import app.tivi.DaggerMvRxFragment
import app.tivi.common.imageloading.loadImageUrl
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.extensions.navigateToNavDestination
import app.tivi.extensions.postponeEnterTransitionWithTimeout
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.home.watched.databinding.FragmentWatchedBinding
import app.tivi.ui.AuthStateMenuItemBinder
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.authStateToolbarMenuBinder
import app.tivi.ui.recyclerview.HideImeOnScrollListener
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class WatchedFragment : DaggerMvRxFragment() {
    private lateinit var binding: FragmentWatchedBinding

    private val viewModel: WatchedViewModel by fragmentViewModel()
    @Inject lateinit var watchedViewModelFactory: WatchedViewModel.Factory

    @Inject lateinit var controller: WatchedEpoxyController
    @Inject lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var authStateMenuItemBinder: AuthStateMenuItemBinder

    private var currentActionMode: ActionMode? = null

    private val listItemSharedElementHelper by lazy(LazyThreadSafetyMode.NONE) {
        ListItemSharedElementHelper(binding.watchedRv)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWatchedBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransitionWithTimeout()

        authStateMenuItemBinder = authStateToolbarMenuBinder(
                binding.watchedToolbar,
                R.id.home_menu_user_avatar,
                R.id.home_menu_user_login
        ) { menuItem, url -> menuItem.loadImageUrl(requireContext(), url) }

        binding.watchedToolbar.apply {
            setupWithNavController(findNavController(), appBarConfiguration)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.home_menu_user_login -> {
                        viewModel.onLoginClicked()
                    }
                    R.id.home_menu_user_avatar -> {
                        findNavController().navigateToNavDestination(R.id.navigation_settings)
                    }
                }
                true
            }
        }

        controller.callbacks = object : WatchedEpoxyController.Callbacks {
            override fun onItemClicked(item: WatchedShowEntryWithShow) {
                // Let the ViewModel have the first go
                if (viewModel.onItemClick(item.show)) {
                    return
                }

                val extras = listItemSharedElementHelper.createForItem(item, "poster") {
                    it.findViewById(R.id.show_poster)
                }

                findNavController().navigate(
                        R.id.activity_show_details,
                        bundleOf("show_id" to item.show.id),
                        null,
                        extras.toActivityNavigatorExtras(requireActivity())
                )
            }

            override fun onItemLongClicked(item: WatchedShowEntryWithShow): Boolean {
                return viewModel.onItemLongClick(item.show)
            }

            override fun onFilterChanged(filter: String) = viewModel.setFilter(filter)

            override fun onSortSelected(sort: SortOption) = viewModel.setSort(sort)
        }

        binding.watchedRv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            addOnScrollListener(HideImeOnScrollListener())
            setController(controller)
        }

        binding.watchedSwipeRefresh.setOnRefreshListener(viewModel::refresh)
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (binding.state == null) {
            // First time we've had state, start any postponed transitions
            scheduleStartPostponedTransitions()
        }

        if (state.selectionOpen && currentActionMode == null) {
            startSelectionActionMode()
        } else if (!state.selectionOpen && currentActionMode != null) {
            currentActionMode?.finish()
        }

        if (currentActionMode != null) {
            currentActionMode?.title = getString(R.string.selection_title,
                    state.selectedShowIds.size)
        }

        binding.state = state

        authStateMenuItemBinder.bind(state.authState, state.user)

        if (state.watchedShows != null) {
            // PagingEpoxyController does not like being updated before it has a list
            controller.viewState = state
            controller.submitList(state.watchedShows)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentActionMode?.finish()
    }

    private fun startSelectionActionMode() {
        currentActionMode = requireActivity().startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.menu_follow -> viewModel.followSelectedShows()
                }
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.action_mode_watched, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = true

            override fun onDestroyActionMode(mode: ActionMode) {
                viewModel.clearSelection()

                if (mode == currentActionMode) {
                    currentActionMode = null
                }
            }
        })
    }
}
