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

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.data.entities.SearchResults
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.ShowDetailed
import androidx.compose.ui.unit.dp
import app.tivi.data.entities.ShowTmdbImage
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun Search(
    state: SearchViewState,
    actioner: (SearchAction) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        state.searchResults?.let { results ->
            SearchList(
                results = results.results,
                onShowClicked = { actioner(SearchAction.OpenShowDetails(it.id)) }
            )
        }
    }
}

@Composable
private fun SearchList(
    results: List<ShowDetailed>,
    onShowClicked: (TiviShow) -> Unit,
) {
    if (results.isNotEmpty()) {
        LazyColumnFor(items = results) { result ->
            SearchRow(
                show = result.show,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowClicked(result.show) }
            )
        }
    } else {
        // TODO
    }
}

@Composable
private fun SearchRow(
    show: TiviShow,
    posterImage: ShowTmdbImage?,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        val imageModifier = Modifier
            .preferredWidth(112.dp)
            .aspectRatio(4 / 3f)

        if (posterImage != null) {
            CoilImage(
                data = posterImage,
                fadeIn = true,
                modifier = imageModifier
            )
        } else Spacer(imageModifier)

        Text(text = show.tit)

    }
}