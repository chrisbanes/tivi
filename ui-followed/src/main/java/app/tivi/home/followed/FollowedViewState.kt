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

package app.tivi.home.followed

import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TraktUser
import app.tivi.trakt.TraktAuthState

internal data class FollowedViewState(
    val user: TraktUser? = null,
    val authState: TraktAuthState = TraktAuthState.LOGGED_OUT,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val selectionOpen: Boolean = false,
    val selectedShowIds: Set<Long> = emptySet(),
    val filterActive: Boolean = false,
    val filter: String? = null,
    val availableSorts: List<SortOption> = emptyList(),
    val sort: SortOption = SortOption.SUPER_SORT
)
