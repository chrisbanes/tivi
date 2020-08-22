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

package app.tivi.home.followed

import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.tivi.FragmentWithBinding
import app.tivi.common.imageloading.loadImageUrl
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.extensions.doOnSizeChange
import app.tivi.extensions.postponeEnterTransitionWithTimeout
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.home.followed.databinding.FragmentFollowedBinding
import app.tivi.ui.AuthStateMenuItemBinder
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.authStateToolbarMenuBinder
import app.tivi.ui.createSharedElementHelperForItem
import app.tivi.ui.recyclerview.HideImeOnScrollListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class FollowedFragment : FragmentWithBinding<FragmentFollowedBinding>() {
    private val viewModel: FollowedViewModel by viewModels()

    @Inject internal lateinit var controller: FollowedEpoxyController

    private var currentActionMode: ActionMode? = null

    private var authStateMenuItemBinder: AuthStateMenuItemBinder? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentFollowedBinding {
        return FragmentFollowedBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(binding: FragmentFollowedBinding, savedInstanceState: Bundle?) {
        postponeEnterTransitionWithTimeout()

        authStateMenuItemBinder = authStateToolbarMenuBinder(
            binding.followedToolbar,
            R.id.home_menu_user_avatar,
            R.id.home_menu_user_login
        ) { menuItem, url -> menuItem.loadImageUrl(requireContext(), url) }

        binding.followedToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.home_menu_user_login, R.id.home_menu_user_avatar -> {
                    findNavController().navigate(R.id.navigation_account)
                    true
                }
                else -> false
            }
        }

        binding.followedAppBar.doOnSizeChange {
            binding.followedRv.updatePadding(top = it.height)
            binding.followedSwipeRefresh.setProgressViewOffset(
                true, 0,
                it.height + binding.followedSwipeRefresh.progressCircleDiameter / 2
            )
            true
        }

        controller.callbacks = object : FollowedEpoxyController.Callbacks {
            override fun onItemClicked(item: FollowedShowEntryWithShow) {
                // Let the ViewModel have the first go
                if (viewModel.onItemClick(item.show)) {
                    return
                }

                val extras = binding.followedRv.createSharedElementHelperForItem(item, "poster") {
                    it.findViewById(R.id.show_poster)
                }

                findNavController().navigate(
                    "app.tivi://show/${item.show.id}".toUri(),
                    null,
                    extras.toActivityNavigatorExtras(requireActivity())
                )
            }

            override fun onItemLongClicked(item: FollowedShowEntryWithShow): Boolean {
                return viewModel.onItemLongClick(item.show)
            }

            override fun onFilterChanged(filter: String) {
                viewModel.setFilter(filter)
            }

            override fun onSortSelected(sort: SortOption) {
                viewModel.setSort(sort)
            }
        }

        binding.followedRv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            addOnScrollListener(HideImeOnScrollListener())
            setController(controller)
        }

        binding.followedSwipeRefresh.setOnRefreshListener(viewModel::refresh)

        lifecycleScope.launchWhenStarted {
            viewModel.pagedList.collect {
                controller.submitList(it)
            }
        }

        viewModel.liveData.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: FollowedViewState) {
        val binding = requireBinding()
        if (binding.state == null) {
            // First time we've had state, start any postponed transitions
            scheduleStartPostponedTransitions()
        }

        if (state.selectionOpen && currentActionMode == null) {
            startSelectionActionMode()
        } else if (!state.selectionOpen && currentActionMode != null) {
            currentActionMode?.finish()
        }

        currentActionMode?.title = getString(R.string.selection_title, state.selectedShowIds.size)

        authStateMenuItemBinder?.bind(state.authState, state.user)

        binding.state = state
        controller.state = state
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentActionMode?.finish()
        controller.clear()
        authStateMenuItemBinder = null
    }

    private fun startSelectionActionMode() {
        currentActionMode = requireActivity().startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.menu_unfollow -> viewModel.unfollowSelectedShows()
                }
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.action_mode_followed, menu)
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
