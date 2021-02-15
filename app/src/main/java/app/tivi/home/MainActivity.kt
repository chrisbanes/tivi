/*
 * Copyright 2017 Google LLC
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

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import app.tivi.R
import app.tivi.TiviActivity
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.databinding.ActivityMainBinding
import app.tivi.extensions.MultipleBackStackNavigation
import app.tivi.extensions.hideSoftInput
import app.tivi.settings.TiviPreferences
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : TiviActivity() {
    private lateinit var viewModel: MainActivityViewModel

    private lateinit var binding: ActivityMainBinding

    var currentNavController: LiveData<NavController>? = null
        private set

    @Inject lateinit var preferences: TiviPreferences

    private var currentSelectedItem by mutableStateOf(HomeNavigation.Discover)

    private lateinit var multiBackStackNavigation: MultipleBackStackNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        multiBackStackNavigation = MultipleBackStackNavigation(
            navGraphIds = intArrayOf(
                R.navigation.discover_nav_graph,
                R.navigation.following_nav_graph,
                R.navigation.watched_nav_graph,
                R.navigation.search_nav_graph
            ),
            fragmentManager = supportFragmentManager,
            containerId = R.id.home_nav_container,
            intent = intent,
            getSelectedItemId = { currentSelectedItem.toNavigationId() },
            setSelectedItemId = { currentSelectedItem = navigationIdToHomeNavigation(it) },
        )

        currentNavController = multiBackStackNavigation.selectedNavController
        currentNavController?.observe(this) { navController ->
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id != R.id.navigation_search) {
                    hideSoftInput()
                }
            }
        }

        binding.homeBottomNavigation.setContent {
            ProvideWindowInsets(consumeWindowInsets = false) {
                TiviTheme(useDarkColors = preferences.shouldUseDarkColors()) {
                    HomeBottomNavigation(
                        selectedNavigation = currentSelectedItem,
                        onNavigationSelected = { item ->
                            if (currentSelectedItem == item) {
                                multiBackStackNavigation.onReselected(item.toNavigationId())
                            } else {
                                currentSelectedItem = item
                                multiBackStackNavigation.onItemSelected(item.toNavigationId())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    override fun onNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: super.onNavigateUp()
    }
}

private fun HomeNavigation.toNavigationId(): Int = when (this) {
    HomeNavigation.Discover -> R.navigation.discover_nav_graph
    HomeNavigation.Following -> R.navigation.following_nav_graph
    HomeNavigation.Watched -> R.navigation.watched_nav_graph
    HomeNavigation.Search -> R.navigation.search_nav_graph
}

private fun navigationIdToHomeNavigation(id: Int): HomeNavigation = when (id) {
    R.navigation.discover_nav_graph -> HomeNavigation.Discover
    R.navigation.following_nav_graph -> HomeNavigation.Following
    R.navigation.watched_nav_graph -> HomeNavigation.Watched
    R.navigation.search_nav_graph -> HomeNavigation.Search
    else -> throw IllegalArgumentException("Navigation graph with id not found: $id")
}
