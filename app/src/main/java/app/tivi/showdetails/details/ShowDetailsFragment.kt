/*
 * Copyright 2018 Google, Inc.
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

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.motion.MotionLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import app.tivi.R
import app.tivi.SharedElementHelper
import app.tivi.TiviFragment
import app.tivi.data.entities.Episode
import app.tivi.data.entities.TiviShow
import app.tivi.databinding.FragmentShowDetailsBinding
import app.tivi.extensions.observeNotNull
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.showdetails.ShowDetailsNavigatorViewModel
import app.tivi.ui.RoundRectViewOutline
import javax.inject.Inject

class ShowDetailsFragment : TiviFragment() {
    companion object {
        private const val KEY_SHOW_ID = "show_id"

        fun create(id: Long): ShowDetailsFragment {
            return ShowDetailsFragment().apply {
                arguments = bundleOf(KEY_SHOW_ID to id)
            }
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: ShowDetailsFragmentViewModel
    private lateinit var controller: ShowDetailsEpoxyController
    private lateinit var showDetailsNavigator: ShowDetailsNavigator

    private lateinit var binding: FragmentShowDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ShowDetailsFragmentViewModel::class.java)
        showDetailsNavigator = ViewModelProviders.of(requireActivity(), viewModelFactory)
                .get(ShowDetailsNavigatorViewModel::class.java)

        arguments?.let {
            viewModel.showId = it.getLong(KEY_SHOW_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentShowDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detailsMotion.setOnApplyWindowInsetsListener { _, insets ->
            val lp = binding.detailsStatusBarAnchor.layoutParams
            lp.height = insets.systemWindowInsetTop
            binding.detailsStatusBarAnchor.requestLayout()

            // Just return insets
            insets
        }

        // Make the MotionLayout draw behind the status bar
        binding.detailsMotion.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        binding.detailsMotion.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) {
                if (binding.detailsFollowFab.y < binding.detailsToolbar.y + binding.detailsToolbar.height) {
                    binding.detailsFollowFab.hide()
                } else {
                    binding.detailsFollowFab.show()
                }

                binding.detailsPoster.visibility = View.VISIBLE
            }

            @SuppressLint("RestrictedApi")
            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                when (currentId) {
                    R.id.end -> {
                        binding.detailsFollowFab.visibility = View.GONE
                        binding.detailsPoster.visibility = View.GONE
                    }
                    R.id.start -> {
                        binding.detailsFollowFab.visibility = View.VISIBLE
                        binding.detailsPoster.visibility = View.VISIBLE
                    }
                }
            }
        })

        binding.detailsPoster.apply {
            clipToOutline = true
            outlineProvider = RoundRectViewOutline
        }

        binding.detailsFollowFab.setOnClickListener {
            viewModel.onToggleMyShowsButtonClicked()
        }

        controller = ShowDetailsEpoxyController(requireContext(), object : ShowDetailsEpoxyController.Callbacks {
            override fun onRelatedShowClicked(show: TiviShow, view: View) {
                viewModel.onRelatedShowClicked(
                        showDetailsNavigator,
                        show,
                        SharedElementHelper().apply {
                            addSharedElement(view, "poster")
                        }
                )
            }

            override fun onEpisodeClicked(episode: Episode, view: View) {
                viewModel.onRelatedShowClicked(showDetailsNavigator, episode)
            }
        })

        binding.detailsRv.setController(controller)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.data.observeNotNull(this, this::update)
    }

    private fun update(viewState: ShowDetailsViewState) {
        binding.state = viewState
        controller.setData(viewState)
        scheduleStartPostponedTransitions()
    }
}