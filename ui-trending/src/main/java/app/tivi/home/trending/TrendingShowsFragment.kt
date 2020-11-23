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

package app.tivi.home.trending

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.tivi.common.compose.AmbientHomeTextCreator
import app.tivi.common.compose.TiviContentSetup
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.paging.collectAsLazyPagingItems
import app.tivi.home.HomeTextCreator
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class TrendingShowsFragment : Fragment() {
    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject internal lateinit var homeTextCreator: HomeTextCreator

    private val pendingActions = Channel<TrendingAction>(Channel.BUFFERED)

    private val viewModel: TrendingShowsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        setContent {
            Providers(
                TiviDateFormatterAmbient provides tiviDateFormatter,
                AmbientHomeTextCreator provides homeTextCreator,
            ) {
                TiviContentSetup {
                    Trending(
                        lazyPagingItems = viewModel.pagedList.collectAsLazyPagingItems { old, new ->
                            old.entry.id == new.entry.id
                        },
                        actioner = { pendingActions.offer(it) },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launchWhenStarted {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is TrendingAction.OpenShowDetails -> {
                        findNavController().navigate("app.tivi://show/${action.showId}".toUri())
                    }
                    // else -> viewModel.submitAction(action)
                }
            }
        }
    }
}
