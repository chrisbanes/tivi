/*
 * Copyright 2019 Google LLC
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

package app.tivi.home.watched

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.TiviContentSetup
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.home.HomeTextCreator
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class WatchedFragment : Fragment() {
    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject internal lateinit var homeTextCreator: HomeTextCreator

    private val viewModel: WatchedViewModel by viewModels()

    private val pendingActions = Channel<WatchedAction>(Channel.BUFFERED)

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
                    val viewState by viewModel.liveData.observeAsState()
                    if (viewState != null) {
                        Watched(
                            state = viewState!!,
                            list = viewModel.pagedList.collectAsLazyPagingItems(),
                            actioner = { pendingActions.offer(it) },
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launchWhenStarted {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    WatchedAction.LoginAction,
                    WatchedAction.OpenUserDetails -> {
                        findNavController().navigate("app.tivi://account".toUri())
                    }
                    is WatchedAction.OpenShowDetails -> {
                        findNavController().navigate("app.tivi://show/${action.showId}".toUri())
                    }
                    else -> viewModel.submitAction(action)
                }
            }
        }
    }
}

val AmbientHomeTextCreator = staticAmbientOf<HomeTextCreator> {
    error("HomeTextCreator not provided")
}
