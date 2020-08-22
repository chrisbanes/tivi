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

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import app.tivi.AppNavigator
import app.tivi.R
import app.tivi.TiviActivity
import app.tivi.databinding.ActivityMainBinding
import app.tivi.extensions.hideSoftInput
import app.tivi.extensions.setupWithNavController
import app.tivi.trakt.TraktConstants
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.Side
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : TiviActivity() {
    private val viewModel: HomeActivityViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    var currentNavController: NavController? = null
        private set

    @Inject lateinit var navigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Insetter.builder()
            .applySystemWindowInsetsToPadding(Side.LEFT or Side.RIGHT)
            .consumeSystemWindowInsets(Insetter.CONSUME_AUTO)
            .applyToView(binding.root)

        Insetter.builder()
            .applySystemWindowInsetsToPadding(Side.BOTTOM)
            .consumeSystemWindowInsets(Insetter.CONSUME_AUTO)
            .applyToView(binding.homeBottomNavigation)

        Insetter.setEdgeToEdgeSystemUiFlags(binding.homeRoot, true)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.liveData.observe(this) { invalidate() }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    fun invalidate() {
    }

    override fun handleIntent(intent: Intent) {
        when (intent.action) {
            TraktConstants.INTENT_ACTION_HANDLE_AUTH_RESPONSE -> {
                navigator.onAuthResponse(intent)
            }
        }
    }

    private fun setupBottomNavigationBar() {
        binding.homeBottomNavigation.setupWithNavController(
            listOf(
                R.navigation.discover_nav_graph,
                R.navigation.watched_nav_graph,
                R.navigation.following_nav_graph,
                R.navigation.search_nav_graph
            ),
            supportFragmentManager,
            R.id.home_nav_container,
            intent
        ).observe(this) { navController ->
            currentNavController = navController

            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id != R.id.navigation_search) {
                    hideSoftInput()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.navigateUp() ?: super.onSupportNavigateUp()
    }
}
