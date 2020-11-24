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

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.tivi.R
import app.tivi.common.compose.IconResource

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
    BottomNavigation(
        modifier = modifier
    ) {
        BottomNavigationItem(
            icon = { IconResource(R.drawable.ic_weekend_black_24dp) },
            label = { Text(stringResource(R.string.discover_title)) },
            selected = selectedNavigation == HomeNavigation.Discover,
            onClick = { onNavigationSelected(HomeNavigation.Discover) },
        )

        BottomNavigationItem(
            icon = {  Icon(Icons.Default.FavoriteBorder) },
            label = { Text(stringResource(R.string.following_shows_title)) },
            selected = selectedNavigation == HomeNavigation.Following,
            onClick = { onNavigationSelected(HomeNavigation.Following) },
        )

        BottomNavigationItem(
            icon = { IconResource(R.drawable.ic_visibility) },
            label = { Text(stringResource(R.string.watched_shows_title)) },
            selected = selectedNavigation == HomeNavigation.Watched,
            onClick = { onNavigationSelected(HomeNavigation.Watched) },
        )

        BottomNavigationItem(
            icon = { Icon(Icons.Default.Search) },
            label = { Text(stringResource(R.string.search_navigation_title)) },
            selected = selectedNavigation == HomeNavigation.Search,
            onClick = { onNavigationSelected(HomeNavigation.Search) },
        )
    }
}
