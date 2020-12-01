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
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.TiviContentSetup
import app.tivi.extensions.viewModelProviderFactoryOf
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.AmbientWindowInsets
import dev.chrisbanes.accompanist.insets.ViewWindowInsetObserver
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
    ): View? = ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        // We use ViewWindowInsetObserver rather than ProvideWindowInsets
        // See: https://github.com/chrisbanes/accompanist/issues/155
        val windowInsets = ViewWindowInsetObserver(this).start()

        setContent {
            Providers(
                AmbientShowDetailsTextCreator provides textCreator,
                AmbientWindowInsets provides windowInsets,
            ) {
                TiviContentSetup {
                    val viewState by viewModel.liveData.observeAsState()
                    if (viewState != null) {
                        LogCompositions("ViewState observeAsState")
                        ShowDetails(viewState!!) {
                            pendingActions.offer(it)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

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
