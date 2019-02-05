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

package app.tivi.home.library.followed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import app.tivi.R
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.databinding.FragmentLibraryFollowedBinding
import app.tivi.home.HomeNavigator
import app.tivi.home.HomeNavigatorViewModel
import app.tivi.home.library.LibraryTextCreator
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class FollowedFragment : TiviMvRxFragment() {

    private lateinit var homeNavigator: HomeNavigator
    private lateinit var binding: FragmentLibraryFollowedBinding
    private lateinit var textCreator: LibraryTextCreator

    private val viewModel: FollowedViewModel by fragmentViewModel()
    @Inject lateinit var followedViewModelFactory: FollowedViewModel.Factory

    @Inject lateinit var dateFormatter: TiviDateFormatter

    private val listItemSharedElementHelper by lazy(LazyThreadSafetyMode.NONE) {
        ListItemSharedElementHelper(binding.followedRv) { it.findViewById(R.id.show_poster) }
    }

    private lateinit var controller: FollowedEpoxyController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeNavigator = ViewModelProviders.of(activity!!, viewModelFactory).get(HomeNavigatorViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = app.tivi.databinding.FragmentLibraryFollowedBinding.inflate(inflater, container, false)
        binding.setLifecycleOwner(viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        textCreator = LibraryTextCreator(requireContext())

        binding.followedRv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        controller = FollowedEpoxyController(object : FollowedEpoxyController.Callbacks {
            override fun onItemClicked(item: FollowedShowEntryWithShow) {
                viewModel.onItemPostedClicked(homeNavigator, item.show,
                        listItemSharedElementHelper.createForItem(item, "poster")
                )
            }
        }, textCreator)

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

    internal fun scrollToTop() {
        binding.followedRv.apply {
            stopScroll()
            smoothScrollToPosition(0)
        }
    }
}
