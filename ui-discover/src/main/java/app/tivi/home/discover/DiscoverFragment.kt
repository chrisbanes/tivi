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
import app.tivi.common.compose.ProvideDisplayInsets
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.util.TiviDateFormatter
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverFragment : Fragment() {
    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject internal lateinit var textCreator: DiscoverTextCreator

    private val viewModel: DiscoverViewModel by viewModels()

    private val pendingActions = Channel<DiscoverAction>(Channel.BUFFERED)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        setContent {
            MdcTheme {
                LogCompositions("MdcTheme")

                Providers(
                    TiviDateFormatterAmbient provides tiviDateFormatter,
                    DiscoverTextCreatorAmbient provides textCreator
                ) {
                    ProvideDisplayInsets {
                        LogCompositions("ProvideInsets")

                        val viewState by viewModel.liveData.observeAsState()
                        if (viewState != null) {
                            Discover(
                                state = viewState!!,
                                actioner = { pendingActions.offer(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // TODO move this once we know how to handle transitions in Compose
        scheduleStartPostponedTransitions()

        lifecycleScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    LoginAction,
                    OpenUserDetails -> findNavController().navigate("app.tivi://account".toUri())
                    is OpenShowDetails -> {
                        var uri = "app.tivi://show/${action.showId}"
                        if (action.episodeId != null) {
                            uri += "/episode/${action.episodeId}"
                        }
                        findNavController().navigate(uri.toUri())
                    }
                    OpenTrendingShows -> findNavController().navigate(R.id.navigation_trending)
                    OpenPopularShows -> findNavController().navigate(R.id.navigation_popular)
                    OpenRecommendedShows -> findNavController().navigate(R.id.navigation_recommended)
                    else -> viewModel.submitAction(action)
                }
            }
        }
    }
}
