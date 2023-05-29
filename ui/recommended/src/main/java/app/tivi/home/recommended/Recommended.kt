// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.recommended

import androidx.compose.runtime.Composable
import app.tivi.common.compose.EntryGrid
import app.tivi.common.compose.viewModel
import app.tivi.common.ui.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias RecommendedShows = @Composable (
    openShowDetails: (showId: Long) -> Unit,
    navigateUp: () -> Unit,
) -> Unit

@Inject
@Composable
fun RecommendedShows(
    viewModelFactory: () -> RecommendedShowsViewModel,
    @Assisted openShowDetails: (showId: Long) -> Unit,
    @Assisted navigateUp: () -> Unit,
) {
    RecommendedShows(
        viewModel = viewModel(factory = viewModelFactory),
        openShowDetails = openShowDetails,
        navigateUp = navigateUp,
    )
}

@Composable
internal fun RecommendedShows(
    viewModel: RecommendedShowsViewModel,
    openShowDetails: (showId: Long) -> Unit,
    navigateUp: () -> Unit,
) {
    val viewState = viewModel.presenter()
    EntryGrid(
        lazyPagingItems = viewState.items,
        title = stringResource(MR.strings.discover_recommended_title),
        onOpenShowDetails = openShowDetails,
        onNavigateUp = navigateUp,
    )
}
