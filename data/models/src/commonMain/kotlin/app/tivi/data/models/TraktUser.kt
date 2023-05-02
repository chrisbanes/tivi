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

package app.tivi.data.models

import kotlinx.datetime.Instant

data class TraktUser(
    override val id: Long = 0,
    val username: String,
    val name: String? = null,
    val joined: Instant? = null,
    val location: String? = null,
    val about: String? = null,
    val avatarUrl: String? = null,
    val vip: Boolean? = null,
    val isMe: Boolean = false,
) : TiviEntity
