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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import app.tivi.DaggerMvRxFragment
import app.tivi.common.epoxy.StickyHeaderScrollListener
import app.tivi.common.imageloading.loadImageUrl
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.doOnApplyWindowInsets
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.extensions.toFragmentNavigatorExtras
import app.tivi.home.discover.databinding.FragmentDiscoverBinding
import app.tivi.ui.AuthStateMenuItemBinder
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.authStateToolbarMenuBinder
import app.tivi.ui.transitions.GridToGridTransitioner
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class DiscoverFragment : DaggerMvRxFragment() {
    private lateinit var binding: FragmentDiscoverBinding

    private lateinit var listItemSharedElementHelper: ListItemSharedElementHelper

    private val viewModel: DiscoverViewModel by fragmentViewModel()
    @Inject lateinit var discoverViewModelFactory: DiscoverViewModel.Factory

    @Inject lateinit var controller: DiscoverEpoxyController
    @Inject lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var authStateMenuItemBinder: AuthStateMenuItemBinder

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
        // postponeEnterTransitionWithTimeout()

        binding.summaryRv.apply {
            setController(controller)
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            addOnScrollListener(
                    StickyHeaderScrollListener(
                            controller,
                            controller::isHeader,
                            binding.headerHolder
                    )
            )
        }

        authStateMenuItemBinder = authStateToolbarMenuBinder(
                binding.discoverToolbar,
                R.id.home_menu_user_avatar,
                R.id.home_menu_user_login
        ) { menuItem, url -> menuItem.loadImageUrl(requireContext(), url) }

        binding.statusScrim.doOnApplyWindowInsets { scrim, insets, _, _ ->
            scrim.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = insets.systemWindowInsetTop
                validate()
            }
        }

        binding.discoverToolbar.apply {
            setupWithNavController(findNavController(), appBarConfiguration)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.home_menu_user_login -> viewModel.onLoginClicked()
                }
                true
            }
        }

        controller.callbacks = object : DiscoverEpoxyController.Callbacks {
            override fun onTrendingHeaderClicked() {
                withState(viewModel) {
                    val extras = listItemSharedElementHelper.createForItems(it.trendingItems)

                    findNavController().navigate(
                            R.id.navigation_trending,
                            null,
                            null,
                            extras.toFragmentNavigatorExtras())
                }
            }

            override fun onPopularHeaderClicked() {
                withState(viewModel) {
                    val extras = listItemSharedElementHelper.createForItems(it.popularItems)

                    findNavController().navigate(
                            R.id.navigation_popular,
                            null,
                            null,
                            extras.toFragmentNavigatorExtras())
                }
            }

            override fun onRecommendedHeaderClicked() {
                withState(viewModel) {
                    val extras = listItemSharedElementHelper.createForItems(it.recommendedItems)

                    findNavController().navigate(
                            R.id.navigation_recommended,
                            null,
                            null,
                            extras.toFragmentNavigatorExtras())
                }
            }

            override fun onItemClicked(viewHolderId: Long, item: EntryWithShow<out Entry>) {
                val elements = listItemSharedElementHelper.createForId(viewHolderId, "poster") {
                    it.findViewById(R.id.show_poster)
                }

                findNavController().navigate(
                        R.id.activity_show_details,
                        bundleOf("show_id" to item.show.id),
                        null,
                        elements.toActivityNavigatorExtras(requireActivity())
                )
            }

            override fun onNextEpisodeToWatchClicked() {
                withState(viewModel) {
                    checkNotNull(it.nextEpisodeWithShowToWatched)

                    findNavController().navigate(
                            R.id.activity_show_details,
                            bundleOf("show_id" to it.nextEpisodeWithShowToWatched.show.id)
                    )
                }
            }
        }

        listItemSharedElementHelper = ListItemSharedElementHelper(binding.summaryRv)

        binding.summarySwipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.summarySwipeRefresh.postOnAnimation {
                binding.summarySwipeRefresh.isRefreshing = false
            }
        }
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (binding.state == null) {
                // First time we've had state, start any postponed transitions
                scheduleStartPostponedTransitions()
            }

            authStateMenuItemBinder.bind(state.authState, state.user)

            binding.state = state
            controller.setData(state)
        }
    }
}
