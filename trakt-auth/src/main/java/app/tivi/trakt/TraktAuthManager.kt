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

package app.tivi.trakt

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

interface TraktAuthManager {
    fun buildActivityResult(): LoginTrakt = LoginTrakt(buildLoginIntent())
    fun buildLoginIntent(): Intent
    fun onAuthResponse(result: LoginTrakt.Result)
}

class LoginTrakt internal constructor(
    private val loginIntent: Intent,
) : ActivityResultContract<Unit, LoginTrakt.Result?>() {
    override fun createIntent(context: Context, input: Unit?): Intent = loginIntent

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Result? = intent?.let {
        Result(
            AuthorizationResponse.fromIntent(it),
            AuthorizationException.fromIntent(it)
        )
    }

    data class Result(
        val response: AuthorizationResponse?,
        val exception: AuthorizationException?
    )
}
