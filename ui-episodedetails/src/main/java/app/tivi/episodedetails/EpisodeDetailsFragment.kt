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

package app.tivi.episodedetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.extensions.viewModelProviderFactoryOf
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.LocalWindowInsets
import dev.chrisbanes.accompanist.insets.ViewWindowInsetObserver
import javax.inject.Inject

@AndroidEntryPoint
class EpisodeDetailsFragment : Fragment() {
    @Inject internal lateinit var vmFactory: EpisodeDetailsViewModel.Factory
    @Inject lateinit var preferences: TiviPreferences

    companion object {
        private const val ARG_KEY_ID = "episode_id"

        @JvmStatic
        fun create(id: Long): EpisodeDetailsFragment {
            return EpisodeDetailsFragment().apply {
                arguments = bundleOf(ARG_KEY_ID to id)
            }
        }
    }

    private val viewModel: EpisodeDetailsViewModel by viewModels {
        viewModelProviderFactoryOf {
            vmFactory.create(requireArguments().getLong(ARG_KEY_ID))
        }
    }

    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject internal lateinit var textCreator: EpisodeDetailsTextCreator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        // We use ViewWindowInsetObserver rather than ProvideWindowInsets
        // See: https://github.com/chrisbanes/accompanist/issues/155
        val windowInsets = ViewWindowInsetObserver(this).start(consumeWindowInsets = false)

        setContent {
            CompositionLocalProvider(
                LocalTiviDateFormatter provides tiviDateFormatter,
                LocalWindowInsets provides windowInsets,
            ) {
                TiviTheme(useDarkColors = preferences.shouldUseDarkColors()) {
                    val viewState by viewModel.liveData.observeAsState()
                    if (viewState != null) {
                        EpisodeDetails(
                            viewState = viewState!!,
                            actioner = ::onEpisodeDetailsAction
                        )
                    }
                }
            }
        }
    }

    private fun onEpisodeDetailsAction(action: EpisodeDetailsAction) {
        when (action) {
            is Close -> requireView().post { findNavController().navigateUp() }
            else -> viewModel.submitAction(action)
        }
    }
}
