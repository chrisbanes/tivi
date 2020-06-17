/*
 * Copyright 2019 Google LLC
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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import app.tivi.AppNavigator
import app.tivi.account.AccountUiFragment
import app.tivi.trakt.TraktConstants
import app.tivi.trakt.TraktManager
import dagger.hilt.android.qualifiers.ActivityContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import javax.inject.Inject
import javax.inject.Provider

class ActivityAppNavigator @Inject constructor(
    @ActivityContext private val context: Context,
    private val traktManager: TraktManager,
    private val requestProvider: Provider<AuthorizationRequest>
) : AppNavigator {
    private val authService by lazy(LazyThreadSafetyMode.NONE) {
        AuthorizationService(context)
    }

    override fun login() {
        authService.performAuthorizationRequest(
            requestProvider.get(),
            provideAuthHandleResponseIntent(0)
        )
    }

    override fun openAccount() {
        // TODO: Move this to use navigation
        AccountUiFragment().show((context as FragmentActivity).supportFragmentManager, "account")
    }

    override fun onAuthResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)
        when {
            response != null -> {
                traktManager.onAuthResponse(authService, response)
            }
            error != null -> {
                traktManager.onAuthException(error)
            }
        }
    }

    override fun provideAuthHandleResponseIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, HomeActivity::class.java).apply {
            action = TraktConstants.INTENT_ACTION_HANDLE_AUTH_RESPONSE
        }
        return PendingIntent.getActivity(context, requestCode, intent, 0)
    }
}
