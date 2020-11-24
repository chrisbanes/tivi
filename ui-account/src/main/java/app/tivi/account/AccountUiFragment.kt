/*
 * Copyright 2020 Google LLC
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

package app.tivi.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.tivi.common.compose.AmbientTiviDateFormatter
import app.tivi.common.compose.TiviContentSetup
import app.tivi.extensions.navigateToNavDestination
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccountUiFragment : DialogFragment() {
    private val pendingActions = Channel<AccountUiAction>()
    private val viewModel: AccountUiViewModel by viewModels()

    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        setContent {
            Providers(AmbientTiviDateFormatter provides tiviDateFormatter) {
                TiviContentSetup {
                    val viewState by viewModel.liveData.observeAsState()
                    if (viewState != null) {
                        AccountUi(viewState!!) {
                            pendingActions.offer(it)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is Close -> view?.post(::dismiss)
                    is OpenSettings -> {
                        view?.post {
                            findNavController().navigateToNavDestination(R.id.navigation_settings)
                        }
                    }
                    else -> viewModel.submitAction(action)
                }
            }
        }
    }
}
