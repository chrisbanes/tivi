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

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.PosterCard
import app.tivi.common.compose.SearchTextField
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.ShowDetailed
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun Search(
    state: SearchViewState,
    actioner: (SearchAction) -> Unit
) {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            val searchBarHeight = remember { mutableStateOf(0) }

            SearchList(
                results = state.searchResults,
                contentPadding = PaddingValues(
                    top = with(LocalDensity.current) { searchBarHeight.value.toDp() }
                ),
                onShowClicked = { actioner(SearchAction.OpenShowDetails(it.id)) }
            )

            Box(
                modifier = Modifier
                    .onSizeChanged { searchBarHeight.value = it.height }
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.95f))
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                var searchQuery by remember { mutableStateOf(TextFieldValue(state.query)) }
                SearchTextField(
                    value = searchQuery,
                    onValueChange = { value ->
                        searchQuery = value
                        actioner(SearchAction.Search(value.text))
                    },
                    hint = stringResource(R.string.search_hint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchList(
    results: List<ShowDetailed>,
    onShowClicked: (TiviShow) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(contentPadding = contentPadding) {
        items(results) { item ->
            SearchRow(
                show = item.show,
                posterImage = item.poster,
                modifier = Modifier
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
    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        PosterCard(
            show = show,
            poster = posterImage,
            modifier = Modifier
                .height(80.dp)
                .aspectRatio(2 / 3f)
        )

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f).align(Alignment.CenterVertically)) {
            Text(
                text = show.title ?: "No title",
                style = MaterialTheme.typography.subtitle1,
            )

            if (show.summary?.isNotEmpty() == true) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = show.summary!!,
                        style = MaterialTheme.typography.caption,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}
