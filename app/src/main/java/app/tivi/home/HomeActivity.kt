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
import androidx.lifecycle.ViewModelProviders
import app.tivi.TiviActivity
import app.tivi.home.main.HomeNavigationFragment
import app.tivi.trakt.TraktConstants
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

class HomeActivity : TiviActivity() {

    private lateinit var viewModel: HomeActivityViewModel

    val authService by lazy(LazyThreadSafetyMode.NONE) {
        AuthorizationService(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(HomeActivityViewModel::class.java)

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, HomeNavigationFragment())
                .commit()
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
}
