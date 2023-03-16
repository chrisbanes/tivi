/*
 * Copyright 2021 Google LLC
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

package app.tivi.data.traktauth

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import me.tatarka.inject.annotations.Inject
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

@Inject
class LoginTraktActivityResultContract(
    private val authService: AuthorizationService,
    private val request: AuthorizationRequest,
) : ActivityResultContract<Unit, LoginTraktActivityResultContract.Result?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return authService.getAuthorizationRequestIntent(request)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Result? = intent?.let {
        Result(
            response = AuthorizationResponse.fromIntent(it),
            exception = AuthorizationException.fromIntent(it),
        )
    }

    data class Result(
        val response: AuthorizationResponse?,
        val exception: AuthorizationException?,
    )
}
