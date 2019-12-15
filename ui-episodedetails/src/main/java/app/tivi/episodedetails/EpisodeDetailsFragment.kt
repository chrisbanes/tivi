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

package app.tivi.episodedetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import app.tivi.TiviFragmentWithBinding
import app.tivi.common.epoxy.SwipeAwayCallbacks
import app.tivi.episodedetails.databinding.FragmentEpisodeDetailsBinding
import app.tivi.extensions.resolveThemeColor
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

class EpisodeDetailsFragment : TiviFragmentWithBinding<FragmentEpisodeDetailsBinding>(),
        EpisodeDetailsViewModel.FactoryProvider {
    companion object {
        @JvmStatic
        fun create(id: Long): EpisodeDetailsFragment {
            return EpisodeDetailsFragment().apply {
                arguments = bundleOf("episode_id" to id)
            }
        }
    }

    private val viewModel: EpisodeDetailsViewModel by fragmentViewModel()

    @Inject internal lateinit var episodeDetailsViewModelFactory: EpisodeDetailsViewModel.Factory
    @Inject internal lateinit var controller: EpisodeDetailsEpoxyController
    @Inject internal lateinit var textCreator: EpisodeDetailsTextCreator

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): FragmentEpisodeDetailsBinding {
        return FragmentEpisodeDetailsBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(binding: FragmentEpisodeDetailsBinding, savedInstanceState: Bundle?) {
        binding.epDetailsRv.setController(controller)

        binding.epDetailsFab.setOnClickListener {
            withState(viewModel) { state ->
                when (state.action) {
                    Action.WATCH -> viewModel.submitAction(AddEpisodeWatchAction)
                    Action.UNWATCH -> viewModel.submitAction(RemoveAllEpisodeWatchesAction)
                }
            }
        }

        val context = requireContext()
        val swipeCallback = object : SwipeAwayCallbacks<EpisodeDetailsWatchItemBindingModel_>(
                context.getDrawable(R.drawable.ic_eye_off_24dp)!!,
                context.resources.getDimensionPixelSize(R.dimen.spacing_large),
                context.getColor(R.color.swipe_away_background),
                context.resolveThemeColor(R.attr.colorSecondary)
        ) {
            override fun onSwipeCompleted(
                model: EpisodeDetailsWatchItemBindingModel_,
                itemView: View,
                position: Int,
                direction: Int
            ) = viewModel.submitAction(RemoveEpisodeWatchAction(model.watch().id))

            override fun isSwipeEnabledForModel(model: EpisodeDetailsWatchItemBindingModel_): Boolean {
                return model.watch() != null
            }
        }

        EpoxyTouchHelper.initSwiping(binding.epDetailsRv)
                .let {
                    if (binding.epDetailsRv.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                        it.right()
                    } else {
                        it.left()
                    }
                }
                .withTarget(EpisodeDetailsWatchItemBindingModel_::class.java)
                .andCallbacks(swipeCallback)
    }

    override fun invalidate(binding: FragmentEpisodeDetailsBinding) = withState(viewModel) { state ->
        binding.state = state
        controller.setData(state)
    }

    override fun provideFactory(): EpisodeDetailsViewModel.Factory = episodeDetailsViewModelFactory
}
