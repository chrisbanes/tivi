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

package app.tivi.home.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.ProvideDisplayInsets
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel

@AndroidEntryPoint
internal class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by viewModels()
    private val pendingActions = Channel<SearchAction>(Channel.BUFFERED)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        setContent {
            Providers() {
                MdcTheme {
                    LogCompositions("MdcTheme")

                    ProvideDisplayInsets {
                        LogCompositions("ProvideInsets")

                        val viewState by viewModel.liveData.observeAsState()
                        if (viewState != null) {
                            LogCompositions("ViewState observeAsState")
                            Search(viewState!!) {
                                pendingActions.offer(it)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pendingActions.close()
    }
}
