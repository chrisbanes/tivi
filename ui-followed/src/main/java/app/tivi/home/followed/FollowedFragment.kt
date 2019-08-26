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
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import app.tivi.TiviMvRxFragment
import app.tivi.common.epoxy.StickyHeaderScrollListener
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.home.followed.databinding.FragmentFollowedBinding
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.recyclerview.HideImeOnScrollListener
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class FollowedFragment : TiviMvRxFragment() {
    private lateinit var binding: FragmentFollowedBinding

    private val viewModel: FollowedViewModel by fragmentViewModel()
    @Inject lateinit var followedViewModelFactory: FollowedViewModel.Factory

    private val listItemSharedElementHelper by lazy(LazyThreadSafetyMode.NONE) {
        ListItemSharedElementHelper(binding.followedRv)
    }

    @Inject lateinit var controller: FollowedEpoxyController

    private var currentActionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFollowedBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        controller.callbacks = object : FollowedEpoxyController.Callbacks {
            override fun onItemClicked(item: FollowedShowEntryWithShow) {
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

            override fun onItemLongClicked(item: FollowedShowEntryWithShow): Boolean {
                return viewModel.onItemLongClick(item.show)
            }

            override fun onFilterChanged(filter: String) = viewModel.setFilter(filter)

            override fun onSortSelected(sort: SortOption) = viewModel.setSort(sort)
        }

        binding.followedRv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            addOnScrollListener(StickyHeaderScrollListener(controller, controller::isHeader, binding.headerHolder))
            addOnScrollListener(HideImeOnScrollListener())
            setController(controller)
        }

        binding.followedSwipeRefresh.setOnRefreshListener(viewModel::refresh)
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (binding.state == null) {
                // First time we've had state, start any postponed transitions
                scheduleStartPostponedTransitions()
            }

            if (state.selectionOpen && currentActionMode == null) {
                startSelectionActionMode()
            } else if (!state.selectionOpen && currentActionMode != null) {
                currentActionMode?.finish()
            }

            binding.state = state

            if (state.followedShows != null) {
                // PagingEpoxyController does not like being updated before it has a list
                controller.viewState = state
                controller.submitList(state.followedShows)
            }
        }
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
