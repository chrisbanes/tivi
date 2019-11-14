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
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.net.toUri
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.navigation.fragment.findNavController
import app.tivi.TiviFragmentWithBinding
import app.tivi.common.epoxy.syncSpanSizes
import app.tivi.data.entities.ActionDate
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.episodedetails.EpisodeDetailsFragment
import app.tivi.extensions.doOnLayouts
import app.tivi.extensions.resolveThemeColor
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.extensions.scrollToItemId
import app.tivi.extensions.sharedElementHelperOf
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.extensions.updateConstraintSets
import app.tivi.showdetails.details.databinding.FragmentShowDetailsBinding
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.parcel.Parcelize
import me.saket.inboxrecyclerview.dimming.TintPainter
import me.saket.inboxrecyclerview.page.PageStateChangeCallbacks
import javax.inject.Inject

class ShowDetailsFragment : TiviFragmentWithBinding<FragmentShowDetailsBinding>() {
    @Parcelize
    internal data class Arguments(val showId: Long, val episodeToExpand: Long?) : Parcelable

    private val viewModel: ShowDetailsFragmentViewModel by fragmentViewModel()
    @Inject lateinit var showDetailsViewModelFactory: ShowDetailsFragmentViewModel.Factory

    @Inject lateinit var controller: ShowDetailsEpoxyController
    @Inject lateinit var textCreator: ShowDetailsTextCreator

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            requireBinding().detailsRv.collapse()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We need to map the arguments bundle to something MvRx understands
        val args = arguments
        args?.putParcelable(MvRx.KEY_ARG, Arguments(
                args.getLong("show_id"),
                args.getLong("episode_id", Long.MIN_VALUE).let { if (it >= 0) it else null }
        ))
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
                val itemId = generateSeasonItemId(focusedSeason.seasonId)
                val handled = binding.detailsRv.scrollToItemId(itemId, focusedSeason.animatedScroll)

                if (!handled) {
                    binding.detailsRv.doOnLayouts {
                        !it.scrollToItemId(itemId, focusedSeason.animatedScroll)
                    }
                }

                viewModel.clearFocusedSeason()
            }
        }

        viewModel.selectSubscribe(
                viewLifecycleOwner,
                ShowDetailsViewState::expandedEpisodeId,
                deliveryMode = uniqueOnly()
        ) { expandedEpisode ->
            if (expandedEpisode != null) {
                childFragmentManager.commitNow {
                    setTransition(FragmentTransaction.TRANSIT_NONE)
                    replace(R.id.details_expanded_pane,
                            EpisodeDetailsFragment.create(expandedEpisode.episodeId))
                }
                binding.detailsExpandedPane.doOnNextLayout {
                    binding.detailsRv.expandItem(
                            generateEpisodeItemId(expandedEpisode.episodeId))
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

            override fun onPageAboutToExpand(expandAnimDuration: Long) {
                // Make sure we're in the collapsed state
                binding.detailsMotion.transitionToState(R.id.show_details_closed)
            }

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