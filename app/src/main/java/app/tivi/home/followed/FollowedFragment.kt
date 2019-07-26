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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.tivi.R
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.databinding.FragmentLibraryFollowedBinding
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.common.epoxy.StickyHeaderScrollListener
import app.tivi.ui.recyclerview.HideImeOnScrollListener
import app.tivi.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class FollowedFragment : TiviMvRxFragment() {
    private lateinit var binding: FragmentLibraryFollowedBinding

    private val viewModel: FollowedViewModel by fragmentViewModel()
    @Inject lateinit var followedViewModelFactory: FollowedViewModel.Factory

    private val listItemSharedElementHelper by lazy(LazyThreadSafetyMode.NONE) {
        ListItemSharedElementHelper(binding.followedRv)
    }

    @Inject lateinit var controller: FollowedEpoxyController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLibraryFollowedBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        controller.callbacks = object : FollowedEpoxyController.Callbacks {
            override fun onItemClicked(item: FollowedShowEntryWithShow) {
                val direction = FollowedFragmentDirections.actionFollowedToActivityShowDetails(item.show.id)

                val extras = listItemSharedElementHelper.createForItem(item, "poster") {
                    it.findViewById(R.id.show_poster)
                }

                findNavController().navigate(
                        direction, extras.toActivityNavigatorExtras(requireActivity())
                )
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

        binding.followedSwipeRefresh.setOnRefreshListener { viewModel.refresh(true) }
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (binding.state == null) {
                // First time we've had state, start any postponed transitions
                scheduleStartPostponedTransitions()
            }

            binding.state = state

            if (state.followedShows != null) {
                // PagingEpoxyController does not like being updated before it has a list
                controller.viewState = state
                controller.submitList(state.followedShows)
            }
        }
    }
}
