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

package app.tivi.home.recommended

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.tivi.common.compose.LocalHomeTextCreator
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.paging.collectAsLazyPagingItems
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.extensions.DefaultNavOptions
import app.tivi.home.HomeTextCreator
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.LocalWindowInsets
import dev.chrisbanes.accompanist.insets.ViewWindowInsetObserver
import javax.inject.Inject

@AndroidEntryPoint
class RecommendedShowsFragment : Fragment() {
    @Inject internal lateinit var tiviDateFormatter: TiviDateFormatter
    @Inject internal lateinit var homeTextCreator: HomeTextCreator
    @Inject lateinit var preferences: TiviPreferences

    private val viewModel: RecommendedShowsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        // This ensures that the viewModel is created now, rather than later after a composition.
        // Just a small optimization to start the data fetch quicker.
        val pagedList = viewModel.pagedList

        // We use ViewWindowInsetObserver rather than ProvideWindowInsets
        // See: https://github.com/chrisbanes/accompanist/issues/155
        val windowInsets = ViewWindowInsetObserver(this).start(consumeWindowInsets = false)

        setContent {
            CompositionLocalProvider(
                LocalTiviDateFormatter provides tiviDateFormatter,
                LocalHomeTextCreator provides homeTextCreator,
                LocalWindowInsets provides windowInsets,
            ) {
                TiviTheme(useDarkColors = preferences.shouldUseDarkColors()) {
                    Recommended(
                        lazyPagingItems = pagedList.collectAsLazyPagingItems { old, new ->
                            old.entry.id == new.entry.id
                        },
                        actioner = ::onRecommendedAction,
                    )
                }
            }
        }
    }

    private fun onRecommendedAction(action: RecommendedAction) {
        when (action) {
            is RecommendedAction.OpenShowDetails -> {
                findNavController().navigate(
                    "app.tivi://show/${action.showId}".toUri(),
                    DefaultNavOptions
                )
            }
        }
    }
}
