/*
 * Copyright 2018 Google LLC
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

package app.tivi.showdetails.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.net.toUri
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.tivi.TiviFragmentWithBinding
import app.tivi.common.epoxy.syncSpanSizes
import app.tivi.data.entities.ActionDate
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.episodedetails.EpisodeDetailsFragment
import app.tivi.extensions.awaitItemIdExists
import app.tivi.extensions.awaitLayout
import app.tivi.extensions.awaitScrollEnd
import app.tivi.extensions.awaitTransitionComplete
import app.tivi.extensions.findItemIdPosition
import app.tivi.extensions.resolveThemeColor
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.extensions.sharedElementHelperOf
import app.tivi.extensions.smoothScrollToItemPosition
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.extensions.updateConstraintSets
import app.tivi.showdetails.details.databinding.FragmentShowDetailsBinding
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import javax.inject.Inject
import kotlinx.coroutines.launch
import me.saket.inboxrecyclerview.dimming.TintPainter
import me.saket.inboxrecyclerview.page.PageStateChangeCallbacks

class ShowDetailsFragment : TiviFragmentWithBinding<FragmentShowDetailsBinding>() {
    private val viewModel: ShowDetailsFragmentViewModel by fragmentViewModel()

    @Inject internal lateinit var showDetailsViewModelFactory: ShowDetailsFragmentViewModel.Factory
    @Inject internal lateinit var controller: ShowDetailsEpoxyController
    @Inject internal lateinit var textCreator: ShowDetailsTextCreator

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            requireBinding().detailsRv.collapse()
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): FragmentShowDetailsBinding {
        return FragmentShowDetailsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(binding: FragmentShowDetailsBinding, savedInstanceState: Bundle?) {
        binding.textCreator = textCreator

        binding.detailsMotion.doOnApplyWindowInsets { v, insets, _ ->
            (v as MotionLayout).updateConstraintSets {
                constrainHeight(R.id.details_status_bar_anchor, insets.systemWindowInsetTop)
            }
        }

        binding.detailsFollowFab.setOnClickListener {
            viewModel.submitAction(FollowShowToggleAction)
        }

        binding.detailsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp() || requireActivity().onNavigateUp()
        }

        binding.detailsToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_refresh -> {
                    viewModel.submitAction(RefreshAction)
                    true
                }
                else -> false
            }
        }

        viewModel.selectSubscribe(
            viewLifecycleOwner,
            ShowDetailsViewState::focusedSeason,
            deliveryMode = uniqueOnly()
        ) { focusedSeason ->
            if (focusedSeason != null) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val seasonItemId = generateSeasonItemId(focusedSeason.seasonId)
                    val seasonItemPosition = controller.adapter.awaitItemIdExists(seasonItemId)
                    binding.detailsRv.smoothScrollToItemPosition(seasonItemPosition)
                }
                viewModel.clearFocusedSeason()
            }
        }

        viewModel.selectSubscribe(
            viewLifecycleOwner,
            ShowDetailsViewState::openEpisodeUiEffect,
            deliveryMode = uniqueOnly()
        ) { expandedEpisode ->
            if (expandedEpisode is ExecutableOpenEpisodeUiEffect) {
                // We can add the fragment to the pane now while waiting for any animations/
                // scrolling to happen
                val episodeFragment = EpisodeDetailsFragment.create(expandedEpisode.episodeId)
                childFragmentManager.commitNow {
                    setTransition(FragmentTransaction.TRANSIT_NONE)
                    replace(R.id.details_expanded_pane, episodeFragment)
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val seasonItemId = generateSeasonItemId(expandedEpisode.seasonId)
                    val episodeItemId = generateEpisodeItemId(expandedEpisode.episodeId)

                    binding.detailsMotion.transitionToState(R.id.show_details_closed)
                    binding.detailsMotion.awaitTransitionComplete(R.id.show_details_closed)

                    controller.adapter.awaitItemIdExists(episodeItemId)
                    val seasonItemPosition = controller.adapter.findItemIdPosition(seasonItemId)

                    binding.detailsRv.smoothScrollToItemPosition(seasonItemPosition)
                    binding.detailsRv.awaitScrollEnd()

                    episodeFragment.requireView().awaitLayout()
                    binding.detailsRv.expandItem(episodeItemId)
                }

                viewModel.clearExpandedEpisode()
            }
        }

        controller.callbacks = object : ShowDetailsEpoxyController.Callbacks {
            override fun onRelatedShowClicked(show: TiviShow, itemView: View) {
                findNavController().navigate(
                    "app.tivi://show/${show.id}".toUri(),
                    null,
                    sharedElementHelperOf(itemView to "poster")
                        .toActivityNavigatorExtras(requireActivity()))
            }

            override fun onEpisodeClicked(episode: Episode, itemView: View) {
                viewModel.submitAction(OpenEpisodeDetails(episode.id))
            }

            override fun onMarkSeasonUnwatched(season: Season) {
                viewModel.submitAction(MarkSeasonUnwatchedAction(season.id))
            }

            override fun onMarkSeasonWatched(season: Season, onlyAired: Boolean, date: ActionDate) {
                viewModel.submitAction(MarkSeasonWatchedAction(season.id, onlyAired, date))
            }

            override fun onExpandSeason(season: Season, itemView: View) {
                viewModel.submitAction(ChangeSeasonExpandedAction(season.id, true))
            }

            override fun onCollapseSeason(season: Season, itemView: View) {
                viewModel.submitAction(ChangeSeasonExpandedAction(season.id, false))
            }

            override fun onMarkSeasonFollowed(season: Season) {
                viewModel.submitAction(ChangeSeasonFollowedAction(season.id, true))
            }

            override fun onMarkSeasonIgnored(season: Season) {
                viewModel.submitAction(ChangeSeasonFollowedAction(season.id, false))
            }

            override fun onMarkPreviousSeasonsIgnored(season: Season) {
                viewModel.submitAction(UnfollowPreviousSeasonsFollowedAction(season.id))
            }
        }

        binding.detailsRv.apply {
            adapter = controller.adapter
            syncSpanSizes(controller)
            setHasFixedSize(true)

            tintPainter = TintPainter.completeList(
                context.resolveThemeColor(R.attr.colorSurface),
                opacity = 0.7f
            )
            expandablePage = binding.detailsExpandedPane
        }

        // Add a listener to enabled/disable the back press callback, depending on the expanded
        // pane state
        binding.detailsExpandedPane.addStateChangeCallbacks(object : PageStateChangeCallbacks {
            override fun onPageAboutToCollapse(collapseAnimDuration: Long) {}

            override fun onPageAboutToExpand(expandAnimDuration: Long) {}

            override fun onPageCollapsed() {
                backPressedCallback.isEnabled = false

                // Remove the episode details fragment to free-up resources
                val episodeFrag = childFragmentManager.findFragmentById(R.id.details_expanded_pane)
                if (episodeFrag != null) {
                    childFragmentManager.commit {
                        setTransition(FragmentTransaction.TRANSIT_NONE)
                        remove(episodeFrag)
                    }
                }

                // Re-enable MotionLayout's motion handling
                binding.detailsMotion.motionEnabled = true
            }

            override fun onPageExpanded() {
                backPressedCallback.isEnabled = true
                // Disable MotionLayout's motion handling while the pane is expanded
                binding.detailsMotion.motionEnabled = false
                binding.detailsMotion.requestLayout()
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun invalidate(binding: FragmentShowDetailsBinding) = withState(viewModel) { state ->
        if (binding.state == null) {
            // First time we've had state, start any postponed transitions
            scheduleStartPostponedTransitions()
        }
        binding.state = state
        controller.state = state
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller.clear()
    }
}
