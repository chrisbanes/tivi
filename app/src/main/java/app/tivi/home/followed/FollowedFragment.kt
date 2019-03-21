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
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.databinding.FragmentLibraryFollowedBinding
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.util.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class FollowedFragment : TiviMvRxFragment() {
    private lateinit var binding: FragmentLibraryFollowedBinding

    private val viewModel: FollowedViewModel by fragmentViewModel()
    @Inject lateinit var followedViewModelFactory: FollowedViewModel.Factory

    private val listItemSharedElementHelper by lazy(LazyThreadSafetyMode.NONE) {
        ListItemSharedElementHelper(binding.followedRv) { it.findViewById(R.id.show_poster) }
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

        binding.followedRv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        controller.callbacks = object : FollowedEpoxyController.Callbacks {
            override fun onItemClicked(item: FollowedShowEntryWithShow) {
                val direction = FollowedFragmentDirections.actionFollowedToActivityShowDetails(item.show.id)
                findNavController().navigate(
                        direction,
                        listItemSharedElementHelper.createForItem(item, "poster")
                                .toActivityNavigatorExtras(requireActivity())
                )
            }
        }

        binding.followedRv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
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

            binding.state = state

            if (state.followedShows != null) {
                // PagingEpoxyController does not like being updated before it has a list
                controller.tmdbImageUrlProvider = state.tmdbImageUrlProvider
                controller.isEmpty = state.isEmpty
                controller.submitList(state.followedShows)
            }
        }
    }
}
