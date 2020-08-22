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
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.tivi.FragmentWithBinding
import app.tivi.common.imageloading.loadImageUrl
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.doOnSizeChange
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.extensions.toFragmentNavigatorExtras
import app.tivi.home.discover.databinding.FragmentDiscoverBinding
import app.tivi.ui.AuthStateMenuItemBinder
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.authStateToolbarMenuBinder
import app.tivi.ui.createSharedElementHelperForItemId
import app.tivi.ui.createSharedElementHelperForItems
import app.tivi.ui.transitions.GridToGridTransitioner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverFragment : FragmentWithBinding<FragmentDiscoverBinding>() {
    private val viewModel: DiscoverViewModel by viewModels()

    @Inject internal lateinit var controller: DiscoverEpoxyController

    private var authStateMenuItemBinder: AuthStateMenuItemBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GridToGridTransitioner.setupFirstFragment(this)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentDiscoverBinding {
        return FragmentDiscoverBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(binding: FragmentDiscoverBinding, savedInstanceState: Bundle?) {
        // Disable transition for now due to https://issuetracker.google.com/129035555
        // postponeEnterTransitionWithTimeout()

        binding.summaryRv.apply {
            setController(controller)
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        binding.followedAppBar.doOnSizeChange {
            binding.summaryRv.updatePadding(top = it.height)
            binding.summarySwipeRefresh.setProgressViewOffset(
                true, 0,
                it.height + binding.summarySwipeRefresh.progressCircleDiameter / 2
            )
            true
        }

        authStateMenuItemBinder = authStateToolbarMenuBinder(
            binding.discoverToolbar,
            R.id.home_menu_user_avatar,
            R.id.home_menu_user_login
        ) { menuItem, url -> menuItem.loadImageUrl(requireContext(), url) }

        binding.discoverToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.home_menu_user_login, R.id.home_menu_user_avatar -> {
                    findNavController().navigate(R.id.navigation_account)
                    true
                }
                else -> false
            }
        }

        controller.callbacks = object : DiscoverEpoxyController.Callbacks {
            override fun onTrendingHeaderClicked() {
                with(viewModel.currentState()) {
                    val extras = binding.summaryRv.createSharedElementHelperForItems(trendingItems)

                    findNavController().navigate(
                        R.id.navigation_trending,
                        null,
                        null,
                        extras.toFragmentNavigatorExtras()
                    )
                }
            }

            override fun onPopularHeaderClicked() {
                with(viewModel.currentState()) {
                    val extras = binding.summaryRv.createSharedElementHelperForItems(popularItems)

                    findNavController().navigate(
                        R.id.navigation_popular,
                        null,
                        null,
                        extras.toFragmentNavigatorExtras()
                    )
                }
            }

            override fun onRecommendedHeaderClicked() {
                with(viewModel.currentState()) {
                    val extras = binding.summaryRv.createSharedElementHelperForItems(recommendedItems)

                    findNavController().navigate(
                        R.id.navigation_recommended,
                        null,
                        null,
                        extras.toFragmentNavigatorExtras()
                    )
                }
            }

            override fun onItemClicked(viewHolderId: Long, item: EntryWithShow<out Entry>) {
                val elements = binding.summaryRv.createSharedElementHelperForItemId(viewHolderId, "poster") {
                    it.findViewById(R.id.show_poster)
                }
                findNavController().navigate(
                    "app.tivi://show/${item.show.id}".toUri(),
                    null,
                    elements.toActivityNavigatorExtras(requireActivity())
                )
            }

            override fun onNextEpisodeToWatchClicked() {
                with(viewModel.currentState()) {
                    checkNotNull(nextEpisodeWithShowToWatched)
                    val show = nextEpisodeWithShowToWatched.show
                    val episode = nextEpisodeWithShowToWatched.episode
                    findNavController().navigate(
                        "app.tivi://show/${show.id}/episode/${episode.id}".toUri()
                    )
                }
            }
        }

        binding.summarySwipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.summarySwipeRefresh.postOnAnimation {
                binding.summarySwipeRefresh.isRefreshing = false
            }
        }

        viewModel.liveData.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: DiscoverViewState) {
        val binding = requireBinding()
        if (binding.state == null) {
            // First time we've had state, start any postponed transitions
            scheduleStartPostponedTransitions()
        }

        authStateMenuItemBinder?.bind(state.authState, state.user)

        binding.state = state
        controller.state = state
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller.clear()
        authStateMenuItemBinder = null
    }
}
