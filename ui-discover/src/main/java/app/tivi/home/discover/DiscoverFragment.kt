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

package app.tivi.home.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.home.discover.databinding.FragmentDiscoverBinding
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.common.epoxy.StickyHeaderScrollListener
import app.tivi.ui.transitions.GridToGridTransitioner
import app.tivi.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class DiscoverFragment : TiviMvRxFragment() {
    private lateinit var binding: FragmentDiscoverBinding

    private lateinit var listItemSharedElementHelper: ListItemSharedElementHelper

    private val viewModel: DiscoverViewModel by fragmentViewModel()
    @Inject lateinit var discoverViewModelFactory: DiscoverViewModel.Factory

    @Inject lateinit var controller: DiscoverEpoxyController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GridToGridTransitioner.setupFirstFragment(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Disable transition for now due to https://issuetracker.google.com/129035555
        // postponeEnterTransition()

        binding.summaryRv.apply {
            setController(controller)
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            addOnScrollListener(StickyHeaderScrollListener(controller, controller::isHeader, binding.headerHolder))
        }

        controller.callbacks = object : DiscoverEpoxyController.Callbacks {
            override fun onTrendingHeaderClicked(items: List<TrendingEntryWithShow>) {
//                val extras = listItemSharedElementHelper.createForItems(items)
//
//                findNavController().navigate(
//                        R.id.action_discover_to_trending,
//                        null,
//                        null,
//                        extras.toFragmentNavigatorExtras())
            }

            override fun onPopularHeaderClicked(items: List<PopularEntryWithShow>) {
//                val extras = listItemSharedElementHelper.createForItems(items)
//
//                findNavController().navigate(
//                        R.id.action_discover_to_popular,
//                        null,
//                        null,
//                        extras.toFragmentNavigatorExtras())
            }

            override fun onItemClicked(viewHolderId: Long, item: EntryWithShow<out Entry>) {
//                val direction = DiscoverFragmentDirections.actionDiscoverToActivityShowDetails(item.show.id)
//
//                val elements = listItemSharedElementHelper.createForId(viewHolderId, "poster") {
//                    it.findViewById(R.id.show_poster)
//                }
//
//                findNavController().navigate(
//                        direction,
//                        elements.toActivityNavigatorExtras(requireActivity())
//                )
            }
        }

        listItemSharedElementHelper = ListItemSharedElementHelper(binding.summaryRv)

        binding.summarySwipeRefresh.setOnRefreshListener(viewModel::refresh)
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (binding.state == null) {
                // First time we've had state, start any postponed transitions
                scheduleStartPostponedTransitions()
            }

            binding.state = state
            controller.setData(state)
        }
    }
}
