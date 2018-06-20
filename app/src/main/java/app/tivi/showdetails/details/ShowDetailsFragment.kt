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

import android.animation.ObjectAnimator
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v7.graphics.Palette
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import app.tivi.GlideApp
import app.tivi.R
import app.tivi.SharedElementHelper
import app.tivi.TiviFragment
import app.tivi.data.entities.Episode
import app.tivi.data.entities.TiviShow
import app.tivi.extensions.loadFromUrl
import app.tivi.extensions.observeK
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.showdetails.ShowDetailsNavigatorViewModel
import app.tivi.ui.GlidePaletteListener
import app.tivi.ui.NoopApplyWindowInsetsListener
import app.tivi.ui.RoundRectViewOutline
import app.tivi.ui.transitions.DrawableAlphaProperty
import app.tivi.util.ScrimUtil
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ShowDetailsFragmentViewModel::class.java)
        showDetailsNavigator = ViewModelProviders.of(requireActivity(), viewModelFactory)
                .get(ShowDetailsNavigatorViewModel::class.java)

        arguments?.let {
            viewModel.showId = it.getLong(KEY_SHOW_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_show_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        details_backdrop.setOnApplyWindowInsetsListener(NoopApplyWindowInsetsListener)

        details_poster.apply {
            clipToOutline = true
            outlineProvider = RoundRectViewOutline
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

        details_rv.setController(controller)

        details_toolbar.apply {
            inflateMenu(R.menu.details_toolbar)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.details_menu_add_myshows -> viewModel.addToMyShows()
                    R.id.details_menu_remove_myshows -> viewModel.removeFromMyShows()
                    else -> TODO()
                }
                true
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.data.observeK(this) {
            it?.let(this::update)
        }
    }

    private var colorSwatch: Palette.Swatch = Palette.Swatch(Color.BLACK, 0)
        set(value) {
            if (field != value) {
                val background = ColorDrawable(value.rgb)
                details_coordinator.background = background
                ObjectAnimator.ofInt(background, DrawableAlphaProperty, 0, 255).start()

                val scrim = ScrimUtil.makeCubicGradientScrimDrawable(value.rgb, 10, Gravity.BOTTOM)
                val drawable = LayerDrawable(arrayOf(scrim)).apply {
                    setLayerGravity(0, Gravity.FILL)
                    setLayerInsetTop(0, details_backdrop.height / 2)
                }
                details_backdrop.foreground = drawable
                ObjectAnimator.ofInt(drawable, DrawableAlphaProperty, 0, 255).start()

                field = value
            }
        }

    private fun update(viewState: ShowDetailsViewState) {
        val show = viewState.show
        val imageProvider = viewState.tmdbImageUrlProvider

        show.tmdbBackdropPath?.let { path ->
            details_backdrop.doOnLayout {
                GlideApp.with(this)
                        .load(imageProvider.getBackdropUrl(path, details_backdrop.width))
                        .thumbnail(GlideApp.with(this).load(imageProvider.getBackdropUrl(path, 0)))
                        .disallowHardwareConfig()
                        .listener(GlidePaletteListener { colorSwatch = it.dominantSwatch!! })
                        .into(details_backdrop)
            }
        }

        show.tmdbPosterPath?.let { path ->
            details_poster.doOnLayout {
                details_poster.loadFromUrl(
                        imageProvider.getPosterUrl(path, 0),
                        imageProvider.getPosterUrl(path, details_poster.width))
            }
        }

        details_toolbar.menu.let {
            val isFollowed = viewState is FollowedShowDetailsViewState
            it.findItem(R.id.details_menu_add_myshows).isVisible = !isFollowed
            it.findItem(R.id.details_menu_remove_myshows).isVisible = isFollowed
        }

        controller.setData(viewState)

        scheduleStartPostponedTransitions()
    }
}