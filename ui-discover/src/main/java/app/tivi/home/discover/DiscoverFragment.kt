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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.extensions.DefaultNavOptions
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.LocalWindowInsets
import dev.chrisbanes.accompanist.insets.ViewWindowInsetObserver
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverFragment : Fragment() {
    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject internal lateinit var textCreator: DiscoverTextCreator
    @Inject lateinit var preferences: TiviPreferences

    private val viewModel: DiscoverViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        // We use ViewWindowInsetObserver rather than ProvideWindowInsets
        // See: https://github.com/chrisbanes/accompanist/issues/155
        val windowInsets = ViewWindowInsetObserver(this).start(consumeWindowInsets = false)

        setContent {
            CompositionLocalProvider(
                LocalTiviDateFormatter provides tiviDateFormatter,
                LocalDiscoverTextCreator provides textCreator,
                LocalWindowInsets provides windowInsets,
            ) {
                TiviTheme(useDarkColors = preferences.shouldUseDarkColors()) {
                    val viewState by viewModel.liveData.observeAsState()
                    if (viewState != null) {
                        Discover(
                            state = viewState!!,
                            actioner = ::onDiscoverAction
                        )
                    }
                }
            }
        }
    }

    private fun onDiscoverAction(action: DiscoverAction) {
        when (action) {
            LoginAction,
            OpenUserDetails -> findNavController().navigate("app.tivi://account".toUri())
            is OpenShowDetails -> {
                var uri = "app.tivi://show/${action.showId}"
                if (action.episodeId != null) {
                    uri += "/episode/${action.episodeId}"
                }
                findNavController().navigate(uri.toUri(), DefaultNavOptions)
            }
            OpenTrendingShows -> {
                findNavController().navigate(
                    R.id.navigation_trending,
                    null,
                    DefaultNavOptions
                )
            }
            OpenPopularShows -> {
                findNavController().navigate(
                    R.id.navigation_popular,
                    null,
                    DefaultNavOptions
                )
            }
            OpenRecommendedShows -> {
                findNavController().navigate(
                    R.id.navigation_recommended,
                    null,
                    DefaultNavOptions
                )
            }
            else -> viewModel.submitAction(action)
        }
    }
}
