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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.extensions.viewModelProviderFactoryOf
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShowDetailsFragment : Fragment() {
    @Inject internal lateinit var vmFactory: ShowDetailsFragmentViewModel.Factory
    @Inject internal lateinit var textCreator: ShowDetailsTextCreator
    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter

    private val pendingActions = Channel<ShowDetailsAction>(Channel.BUFFERED)

    private val viewModel: ShowDetailsFragmentViewModel by viewModels {
        viewModelProviderFactoryOf {
            val args = requireArguments()
            vmFactory.create(
                showId = args.getLong("show_id"),
                pendingEpisodeId = when {
                    args.containsKey("episode_id") -> args.getLong("episode_id")
                    else -> null
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            composeShowDetails(
                viewModel.liveData,
                { pendingActions.offer(it) },
                tiviDateFormatter,
                textCreator
            )
        }
    }

    override fun onStart() {
        super.onStart()
        // TODO move this once we know how to handle transitions in Compose
        scheduleStartPostponedTransitions()

        viewModel.liveData.observe(this, ::render)

        lifecycleScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    NavigateUp -> findNavController().navigateUp()
                    else -> viewModel.submitAction(action)
                }
            }
        }
    }

    private fun render(state: ShowDetailsViewState) {
        state.pendingUiEffects.forEach { effect ->
            when (effect) {
                is OpenShowUiEffect -> {
                    findNavController().navigate("app.tivi://show/${effect.showId}".toUri())
                    viewModel.submitAction(ClearPendingUiEffect(effect))
                }
                is OpenEpisodeUiEffect -> {
                    findNavController().navigate("app.tivi://episode/${effect.episodeId}".toUri())
                    viewModel.submitAction(ClearPendingUiEffect(effect))
                }
            }
        }
    }
}
