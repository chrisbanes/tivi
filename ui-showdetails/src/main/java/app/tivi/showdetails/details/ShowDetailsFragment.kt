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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.extensions.DefaultNavOptions
import app.tivi.extensions.viewModelProviderFactoryOf
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.LocalWindowInsets
import dev.chrisbanes.accompanist.insets.ViewWindowInsetObserver
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ShowDetailsFragment : Fragment() {
    @Inject internal lateinit var vmFactory: ShowDetailsFragmentViewModel.Factory
    @Inject internal lateinit var textCreator: ShowDetailsTextCreator
    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject lateinit var preferences: TiviPreferences

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.uiEffects.collect { effect ->
                when (effect) {
                    is OpenShowUiEffect -> {
                        findNavController().navigate(
                            "app.tivi://show/${effect.showId}".toUri(),
                            DefaultNavOptions
                        )
                    }
                    is OpenEpisodeUiEffect -> {
                        findNavController().navigate(
                            "app.tivi://episode/${effect.episodeId}".toUri(),
                            navOptions {
                                anim {
                                    enter = R.anim.tivi_enter_bottom_anim
                                    popExit = R.anim.tivi_exit_bottom_anim
                                }
                            }
                        )
                    }
                    else -> {
                        // TODO: any remaining ui effects need to be passed down to the UI
                    }
                }
            }
        }
    }

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
                LocalShowDetailsTextCreator provides textCreator,
                LocalWindowInsets provides windowInsets,
            ) {
                TiviTheme(useDarkColors = preferences.shouldUseDarkColors()) {
                    val viewState by viewModel.liveData.observeAsState()
                    if (viewState != null) {
                        LogCompositions("ViewState observeAsState")
                        ShowDetails(
                            viewState = viewState!!,
                            actioner = ::onShowDetailsAction
                        )
                    }
                }
            }
        }
    }

    private fun onShowDetailsAction(action: ShowDetailsAction) {
        when (action) {
            NavigateUp -> findNavController().navigateUp()
            else -> viewModel.submitAction(action)
        }
    }
}
