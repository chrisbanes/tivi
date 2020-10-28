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

package app.tivi.home.followed

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AmbientEmphasisLevels
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.onSizeChanged
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import app.tivi.common.compose.AbsoluteElevationSurface
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.IconResource
import app.tivi.common.compose.rememberMutableState
import app.tivi.common.compose.spacerItem
import app.tivi.common.compose.statusBarsPadding
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.trakt.TraktAuthState
import dev.chrisbanes.accompanist.coil.CoilImage

@OptIn(ExperimentalLazyDsl::class)
@Composable
fun Followed(
    state: FollowedViewState,
    list: LazyPagingItems<FollowedShowEntryWithShow>,
    actioner: (FollowedAction) -> Unit
) {
    AbsoluteElevationSurface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            var appBarHeight by rememberMutableState { 0 }

            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    val height = with(DensityAmbient.current) { appBarHeight.toDp() }
                    Spacer(Modifier.preferredHeight(height))
                }

                spacerItem(16.dp)

                items(list) { entry ->
                    // TODO entry
                    if (entry != null) {
                        Box(Modifier.fillMaxWidth()) {
                            Text(text = entry.show.title ?: "")
                        }
                    } else {
                        // TODO placeholder?
                    }
                }

                spacerItem(16.dp)
            }

            FollowedAppBar(
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                refreshing = state.isLoading,
                onRefreshActionClick = { actioner(RefreshAction) },
                onUserActionClick = { actioner(OpenUserDetails) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { appBarHeight = it.height }
            )
        }
    }
}

private const val TranslucentAppBarAlpha = 0.93f

@Composable
private fun FollowedAppBar(
    loggedIn: Boolean,
    user: TraktUser?,
    refreshing: Boolean,
    onRefreshActionClick: () -> Unit,
    onUserActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AbsoluteElevationSurface(
        color = MaterialTheme.colors.surface.copy(alpha = TranslucentAppBarAlpha),
        contentColor = MaterialTheme.colors.onSurface,
        elevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .preferredHeight(56.dp)
                .padding(start = 16.dp, end = 4.dp)
        ) {
            ProvideEmphasis(AmbientEmphasisLevels.current.high) {
                Text(
                    text = stringResource(R.string.following_shows_title),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            Spacer(Modifier.weight(1f))

            ProvideEmphasis(AmbientEmphasisLevels.current.medium) {
                IconButton(
                    onClick = onRefreshActionClick,
                    enabled = !refreshing,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    if (refreshing) {
                        AutoSizedCircularProgressIndicator(Modifier.preferredSize(20.dp))
                    } else {
                        Icon(Icons.Default.Refresh)
                    }
                }

                IconButton(
                    onClick = onUserActionClick,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    when {
                        loggedIn && user?.avatarUrl != null -> {
                            CoilImage(
                                data = user.avatarUrl!!,
                                modifier = Modifier.preferredSize(32.dp).clip(CircleShape)
                            )
                        }
                        loggedIn -> IconResource(R.drawable.ic_person)
                        else -> IconResource(R.drawable.ic_person_outline)
                    }
                }
            }
        }
    }
}
