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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.PlaceholderPosterCard
import app.tivi.common.compose.PosterCard
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.itemsInGrid
import app.tivi.common.compose.paging.LazyPagingItems
import app.tivi.data.resultentities.TrendingEntryWithShow
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun Trending(
    lazyPagingItems: LazyPagingItems<TrendingEntryWithShow>,
    actioner: (TrendingAction) -> Unit
) {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            var appBarHeight by remember { mutableStateOf(0) }

            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    val height = with(LocalDensity.current) { appBarHeight.toDp() }
                    Spacer(Modifier.height(height))
                }

                itemsInGrid(
                    lazyPagingItems = lazyPagingItems,
                    columns = 3,
                    contentPadding = PaddingValues(4.dp),
                    verticalItemPadding = 2.dp,
                    horizontalItemPadding = 2.dp
                ) { entry ->
                    val modifier = Modifier
                        .aspectRatio(2 / 3f)
                        .fillMaxWidth()
                    if (entry != null) {
                        PosterCard(
                            show = entry.show,
                            poster = entry.poster,
                            onClick = { actioner(TrendingAction.OpenShowDetails(entry.show.id)) },
                            modifier = modifier
                        )
                    } else {
                        PlaceholderPosterCard(modifier = modifier)
                    }
                }

                if (lazyPagingItems.loadState.append == LoadState.Loading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    }
                }

                itemSpacer(16.dp)
            }

            TrendingAppBar(
                refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading,
                onRefreshActionClick = { lazyPagingItems.refresh() },
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { appBarHeight = it.height }
            )
        }
    }
}

private const val TranslucentAppBarAlpha = 0.93f

@Composable
private fun TrendingAppBar(
    refreshing: Boolean,
    onRefreshActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface.copy(alpha = TranslucentAppBarAlpha),
        contentColor = MaterialTheme.colors.onSurface,
        elevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(56.dp)
                .padding(start = 16.dp, end = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.discover_trending_title),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.weight(1f))

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                IconButton(
                    onClick = onRefreshActionClick,
                    enabled = !refreshing,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    if (refreshing) {
                        AutoSizedCircularProgressIndicator(Modifier.size(20.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh)
                        )
                    }
                }
            }
        }
    }
}
