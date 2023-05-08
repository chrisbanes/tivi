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

package app.tivi.home.trending

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.EntryGrid
import app.tivi.common.compose.viewModel
import app.tivi.common.ui.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias TrendingShows = @Composable (
    openShowDetails: (showId: Long) -> Unit,
    navigateUp: () -> Unit,
) -> Unit

@Inject
@Composable
fun TrendingShows(
    viewModelFactory: () -> TrendingShowsViewModel,
    @Assisted openShowDetails: (showId: Long) -> Unit,
    @Assisted navigateUp: () -> Unit,
) {
    TrendingShows(
        viewModel = viewModel(factory = viewModelFactory),
        openShowDetails = openShowDetails,
        navigateUp = navigateUp,
    )
}

@Composable
internal fun TrendingShows(
    viewModel: TrendingShowsViewModel,
    openShowDetails: (showId: Long) -> Unit,
    navigateUp: () -> Unit,
) {
    EntryGrid(
        lazyPagingItems = viewModel.pagedList.collectAsLazyPagingItems(),
        title = stringResource(MR.strings.discover_trending_title),
        onOpenShowDetails = openShowDetails,
        onNavigateUp = navigateUp,
        modifier = Modifier.fillMaxSize(),
    )
}
