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

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.motion.MotionLayout
import android.support.design.shape.CutCornerTreatment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import app.tivi.R
import app.tivi.SharedElementHelper
import app.tivi.TiviFragment
import app.tivi.data.entities.Episode
import app.tivi.data.entities.TiviShow
import app.tivi.databinding.FragmentShowDetailsBinding
import app.tivi.extensions.materialShapeDrawableOf
import app.tivi.extensions.observeNotNull
import app.tivi.extensions.resolveColor
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.showdetails.ShowDetailsNavigatorViewModel
import app.tivi.ui.RoundRectViewOutline
import kotlinx.android.synthetic.main.fragment_show_details.*
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

        details_motion.setOnApplyWindowInsetsListener { _, insets ->
            val lp = details_status_bar_anchor.layoutParams
            lp.height = insets.systemWindowInsetTop
            details_status_bar_anchor.requestLayout()

            // Just return insets
            insets
        }

        details_motion.setTransitionListener(object : MotionLayout.TransitionListener {
            val fab = details_follow_fab
            override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) {
                if (fab.y < details_toolbar.y + details_toolbar.height) {
                    fab.hide()
                } else {
                    fab.show()
                }

                details_poster.visibility = View.VISIBLE
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                when (currentId) {
                    R.id.end -> {
                        fab.visibility = View.GONE
                        details_poster.visibility = View.GONE
                    }
                    R.id.start -> {
                        fab.visibility = View.VISIBLE
                        details_poster.visibility = View.VISIBLE
                    }
                }
            }
        })

        // Make the MotionLayout draw behind the status bar
        details_motion.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        details_poster.apply {
            clipToOutline = true
            outlineProvider = RoundRectViewOutline
        }

        // Need to update the pivot so that it is top center
        details_poster.doOnLayout {
            it.pivotX = it.width / 2f
            it.pivotY = 0f
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

        val shapeDrawable = materialShapeDrawableOf {
            topLeftCorner = CutCornerTreatment(resources.getDimension(R.dimen.details_corner_cutout))
        }
        shapeDrawable.shadowElevation = resources.getDimensionPixelSize(R.dimen.details_card_elevation)
        shapeDrawable.isShadowEnabled = true
        shapeDrawable.setTint(view.context.theme.resolveColor(android.R.attr.colorBackground))
        details_title.background = shapeDrawable

        details_rv.setController(controller)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.data.observeNotNull(this, this::update)
    }

    private fun update(viewState: ShowDetailsViewState) {
        binding.state = viewState

        val isFollowed = viewState is FollowedShowDetailsViewState
        details_follow_fab.isChecked = isFollowed

        details_follow_fab.setOnClickListener {
            if (isFollowed) {
                viewModel.removeFromMyShows()
            } else {
                viewModel.addToMyShows()
            }
        }

        controller.setData(viewState)

        scheduleStartPostponedTransitions()
    }
}