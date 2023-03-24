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

package app.tivi.home.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.Layout
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.ui.EmptyContent
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.SearchTextField
import app.tivi.common.compose.ui.plus
import app.tivi.common.compose.viewModel
import app.tivi.common.ui.resources.MR
import app.tivi.data.models.TiviShow
import dev.icerock.moko.resources.compose.stringResource
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias Search = @Composable (
    openShowDetails: (showId: Long) -> Unit,
) -> Unit

@Inject
@Composable
fun Search(
    viewModelFactory: () -> SearchViewModel,
    @Assisted openShowDetails: (showId: Long) -> Unit,
) {
    Search(
        viewModel = viewModel(factory = viewModelFactory),
        openShowDetails = openShowDetails,
    )
}

@Composable
internal fun Search(
    viewModel: SearchViewModel,
    openShowDetails: (showId: Long) -> Unit,
) {
    val viewState by viewModel.state.collectAsState()

    Search(
        state = viewState,
        openShowDetails = openShowDetails,
        onSearchQueryChanged = viewModel::search,
        onMessageShown = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Search(
    state: SearchViewState,
    openShowDetails: (showId: Long) -> Unit,
    onSearchQueryChanged: (query: String) -> Unit,
    onMessageShown: (id: Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val dismissSnackbarState = rememberDismissState(
        confirmValueChange = { value ->
            if (value != DismissValue.Default) {
                snackbarHostState.currentSnackbarData?.dismiss()
                true
            } else {
                false
            }
        },
    )

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
            onMessageShown(message.id)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    Modifier
                        .padding(horizontal = Layout.bodyMargin, vertical = 8.dp)
                        .statusBarsPadding()
                        .bodyWidth(),
                ) {
                    var searchQuery by remember { mutableStateOf(TextFieldValue(state.query)) }
                    SearchTextField(
                        value = searchQuery,
                        onValueChange = { value ->
                            searchQuery = value
                            onSearchQueryChanged(value.text)
                        },
                        hint = stringResource(MR.strings.search_hint),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                SwipeToDismiss(
                    state = dismissSnackbarState,
                    background = {},
                    dismissContent = { Snackbar(snackbarData = data) },
                    modifier = Modifier
                        .padding(horizontal = Layout.bodyMargin)
                        .fillMaxWidth(),
                )
            }
        },
    ) { padding ->
        if (state.searchResults.isEmpty() && !state.refreshing) {
            EmptyContent(
                title = {
                    if (state.query.isEmpty()) {
                        Text(text = stringResource(UiR.string.search_empty_title))
                    } else {
                        Text(text = stringResource(UiR.string.search_noresults_title))
                    }
                },
                prompt = {
                    if (state.query.isNotEmpty()) {
                        Text(text = stringResource(UiR.string.search_noresults_prompt))
                    }
                },
                graphic = { Text(text = "\uD83D\uDD75️\u200D♂️") },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            SearchList(
                results = state.searchResults,
                contentPadding = padding + PaddingValues(horizontal = Layout.bodyMargin),
                onShowClicked = { openShowDetails(it.id) },
                modifier = Modifier.bodyWidth(),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchList(
    results: List<TiviShow>,
    onShowClicked: (TiviShow) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val arrangement = Arrangement.spacedBy(Layout.gutter)

    LazyVerticalGrid(
        columns = GridCells.Fixed(Layout.columns / 4),
        contentPadding = contentPadding,
        verticalArrangement = arrangement,
        horizontalArrangement = arrangement,
        modifier = modifier,
    ) {
        items(
            items = results,
            key = { it.id },
        ) { show ->
            SearchRow(
                show = show,
                modifier = Modifier
                    .animateItemPlacement()
                    .fillMaxWidth()
                    .clickable { onShowClicked(show) },
            )
        }
    }
}

@Composable
private fun SearchRow(
    show: TiviShow,
    modifier: Modifier = Modifier,
) {
    Row(modifier.padding(vertical = 8.dp)) {
        PosterCard(
            show = show,
            modifier = Modifier
                .fillMaxWidth(0.2f) // 20% of width
                .aspectRatio(2 / 3f),
        )

        Spacer(Modifier.width(16.dp))

        Column(
            Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
        ) {
            Text(
                text = show.title ?: "No title",
                style = MaterialTheme.typography.titleMedium,
            )

            if (!show.summary.isNullOrEmpty()) {
                Text(
                    text = show.summary!!,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
            }
        }
    }
}
