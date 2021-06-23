/*
 * Copyright 2020 Google LLC
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

internal sealed class FollowedAction {
    object RefreshAction : FollowedAction()
    object LoginAction : FollowedAction()
    data class FilterShows(val filter: String = "") : FollowedAction()
    data class ChangeSort(val sort: SortOption) : FollowedAction()
    object OpenUserDetails : FollowedAction()
    data class OpenShowDetails(val showId: Long) : FollowedAction()
}
