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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import app.tivi.common.compose.Layout
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.SearchTextField
import app.tivi.common.compose.ui.SwipeDismissSnackbarHost
import app.tivi.common.compose.ui.plus
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.ShowDetailed
import com.google.accompanist.insets.ui.Scaffold
import app.tivi.common.ui.resources.R as UiR

@Composable
fun Search(
    openShowDetails: (showId: Long) -> Unit
) {
    Search(
        viewModel = hiltViewModel(),
        openShowDetails = openShowDetails
    )
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
internal fun Search(
    viewModel: SearchViewModel,
    openShowDetails: (showId: Long) -> Unit
) {
    val viewState by viewModel.state.collectAsState()

    Search(
        state = viewState,
        openShowDetails = openShowDetails,
        onSearchQueryChanged = viewModel::search,
        onMessageShown = viewModel::clearMessage
    )
}

@Composable
internal fun Search(
    state: SearchViewState,
    openShowDetails: (showId: Long) -> Unit,
    onSearchQueryChanged: (query: String) -> Unit,
    onMessageShown: (id: Long) -> Unit
) {
    val scaffoldState = rememberScaffoldState()

    state.message?.let { message ->
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message.message)
            onMessageShown(message.id)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Surface(
                color = MaterialTheme.colors.surface.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colors.onSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .padding(horizontal = Layout.bodyMargin, vertical = 8.dp)
                        .statusBarsPadding()
                        .bodyWidth()
                ) {
                    var searchQuery by remember { mutableStateOf(TextFieldValue(state.query)) }
                    SearchTextField(
                        value = searchQuery,
                        onValueChange = { value ->
                            searchQuery = value
                            onSearchQueryChanged(value.text)
                        },
                        hint = stringResource(UiR.string.search_hint),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        snackbarHost = { snackbarHostState ->
            SwipeDismissSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(horizontal = Layout.bodyMargin)
                    .fillMaxWidth()
            )
        }
    ) { padding ->
        SearchList(
            results = state.searchResults,
            contentPadding = padding + PaddingValues(horizontal = Layout.bodyMargin),
            onShowClicked = { openShowDetails(it.id) },
            modifier = Modifier.bodyWidth()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchList(
    results: List<ShowDetailed>,
    onShowClicked: (TiviShow) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val arrangement = Arrangement.spacedBy(Layout.gutter)

    LazyVerticalGrid(
        columns = GridCells.Fixed(Layout.columns / 4),
        contentPadding = contentPadding,
        verticalArrangement = arrangement,
        horizontalArrangement = arrangement,
        modifier = modifier
    ) {
        items(
            items = results,
            key = { it.show.id }
        ) { item ->
            SearchRow(
                show = item.show,
                posterImage = item.poster,
                modifier = Modifier
                    .animateItemPlacement()
                    .fillMaxWidth()
                    .clickable { onShowClicked(item.show) }
            )
        }
    }
}

@Composable
private fun SearchRow(
    show: TiviShow,
    posterImage: ShowTmdbImage?,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(vertical = 8.dp)) {
        PosterCard(
            show = show,
            poster = posterImage,
            modifier = Modifier
                .fillMaxWidth(0.2f) // 20% of width
                .aspectRatio(2 / 3f)
        )

        Spacer(Modifier.width(16.dp))

        Column(
            Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = show.title ?: "No title",
                style = MaterialTheme.typography.subtitle1
            )

            if (show.summary?.isNotEmpty() == true) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = show.summary!!,
                        style = MaterialTheme.typography.caption,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
