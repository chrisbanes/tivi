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

package app.tivi.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.tivi.R
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

internal enum class HomeNavigation {
    Discover,
    Following,
    Watched,
    Search,
}

@Composable
internal fun HomeBottomNavigation(
    selectedNavigation: HomeNavigation,
    onNavigationSelected: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface,
        contentColor = contentColorFor(MaterialTheme.colors.surface),
        elevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_weekend_black_24dp),
                        contentDescription = stringResource(R.string.cd_discover_title)
                    )
                },
                label = { Text(stringResource(R.string.discover_title)) },
                selected = selectedNavigation == HomeNavigation.Discover,
                onClick = { onNavigationSelected(HomeNavigation.Discover) },
            )

            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_following_shows_title)
                    )
                },
                label = { Text(stringResource(R.string.following_shows_title)) },
                selected = selectedNavigation == HomeNavigation.Following,
                onClick = { onNavigationSelected(HomeNavigation.Following) },
            )

            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_visibility),
                        contentDescription = stringResource(R.string.cd_watched_shows_title)
                    )
                },
                label = { Text(stringResource(R.string.watched_shows_title)) },
                selected = selectedNavigation == HomeNavigation.Watched,
                onClick = { onNavigationSelected(HomeNavigation.Watched) },
            )

            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.cd_search_navigation_title)
                    )
                },
                label = { Text(stringResource(R.string.search_navigation_title)) },
                selected = selectedNavigation == HomeNavigation.Search,
                onClick = { onNavigationSelected(HomeNavigation.Search) },
            )
        }
    }
}
