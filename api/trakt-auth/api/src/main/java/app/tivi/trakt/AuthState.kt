/*
 * Copyright 2023 Google LLC
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

interface AuthState {
    val accessToken: String?
    val refreshToken: String?
    val isAuthorized: Boolean
    fun serializeToJson(): String

    companion object {
        val Empty: AuthState = object : AuthState {
            override val accessToken: String? = null
            override val refreshToken: String? = null
            override val isAuthorized: Boolean = false
            override fun serializeToJson(): String = "{}"
        }
    }
}
