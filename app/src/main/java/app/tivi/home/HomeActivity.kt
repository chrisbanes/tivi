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
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.navigation.NavController
import app.tivi.R
import app.tivi.TiviActivityMvRxView
import app.tivi.databinding.ActivityHomeBinding
import app.tivi.extensions.hideSoftInput
import app.tivi.extensions.setupWithNavController
import app.tivi.trakt.TraktConstants
import com.airbnb.mvrx.viewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import javax.inject.Inject

class HomeActivity : TiviActivityMvRxView() {
    private val authService by lazy(LazyThreadSafetyMode.NONE) { AuthorizationService(this) }

    private val viewModel: HomeActivityViewModel by viewModel()

    @Inject lateinit var homeNavigationViewModelFactory: HomeActivityViewModel.Factory

    private lateinit var binding: ActivityHomeBinding

    private var currentNavController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.homeRoot.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.subscribe(this) { postInvalidate() }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    override fun invalidate() {
    }

    override fun handleIntent(intent: Intent) {
        when (intent.action) {
            TraktConstants.INTENT_ACTION_HANDLE_AUTH_RESPONSE -> {
                val response = AuthorizationResponse.fromIntent(intent)
                val error = AuthorizationException.fromIntent(intent)
                viewModel.onAuthResponse(authService, response, error)
            }
        }
    }

    internal fun startLogin() {
        viewModel.onLoginItemClicked(authService)
    }

    private fun setupBottomNavigationBar() {
        binding.homeBottomNavigation.setupWithNavController(
                listOf(R.navigation.discover_nav_graph, R.navigation.watched_nav_graph,
                        R.navigation.following_nav_graph, R.navigation.search_nav_graph),
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
