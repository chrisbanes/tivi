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
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.tivi.extensions.navigateToNavDestination
import app.tivi.util.TiviDateFormatter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccountUiFragment : BottomSheetDialogFragment() {
    private val pendingActions = Channel<AccountUiAction>()
    private val viewModel: AccountUiViewModel by viewModels()

    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FrameLayout(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        composeAccountUi(
            this,
            viewModel.liveData,
            { pendingActions.offer(it) },
            tiviDateFormatter
        )
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
