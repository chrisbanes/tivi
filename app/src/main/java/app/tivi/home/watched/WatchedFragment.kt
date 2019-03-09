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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.tivi.R
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.databinding.FragmentLibraryWatchedBinding
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.util.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class WatchedFragment : TiviMvRxFragment() {
    private lateinit var binding: FragmentLibraryWatchedBinding

    private val viewModel: WatchedViewModel by fragmentViewModel()
    @Inject lateinit var watchedViewModelFactory: WatchedViewModel.Factory

    @Inject lateinit var controller: WatchedEpoxyController

    private val listItemSharedElementHelper by lazy(LazyThreadSafetyMode.NONE) {
        ListItemSharedElementHelper(binding.watchedRv) { it.findViewById(R.id.show_poster) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLibraryWatchedBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        controller.callbacks = object : WatchedEpoxyController.Callbacks {
            override fun onItemClicked(item: WatchedShowEntryWithShow) {
//                viewModel.onItemPostedClicked(homeNavigator, item.show,
//                        listItemSharedElementHelper.createForItem(item, "poster")
//                )
                // TODO
            }
        }

        binding.watchedRv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            setController(controller)
        }

        binding.watchedSwipeRefresh.setOnRefreshListener(viewModel::refresh)
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (binding.state == null) {
                // First time we've had state, start any postponed transitions
                scheduleStartPostponedTransitions()
            }

            binding.state = state

            if (state.watchedShows != null) {
                // PagingEpoxyController does not like being updated before it has a list
                controller.tmdbImageUrlProvider = state.tmdbImageUrlProvider
                controller.isEmpty = state.isEmpty
                controller.submitList(state.watchedShows)
            }
        }
    }
}
