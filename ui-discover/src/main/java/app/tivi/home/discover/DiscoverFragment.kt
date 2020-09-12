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
import android.widget.FrameLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.util.TiviDateFormatter
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
    ): View? {
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            composeDiscover(
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

        lifecycleScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    LoginAction,
                    OpenUserDetails -> findNavController().navigate(R.id.navigation_account)
                    is OpenShowDetails -> {
                        findNavController().navigate("app.tivi://show/${action.showId}".toUri())
                    }
                    else -> viewModel.submitAction(action)
                }
            }
        }
    }
}
