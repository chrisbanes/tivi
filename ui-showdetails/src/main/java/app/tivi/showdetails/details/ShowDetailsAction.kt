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

package app.tivi.showdetails.details

import app.tivi.data.entities.ActionDate

internal sealed class ShowDetailsAction {
    object RefreshAction : ShowDetailsAction()
    object FollowShowToggleAction : ShowDetailsAction()

    data class MarkSeasonWatchedAction(
        val seasonId: Long,
        val onlyAired: Boolean = false,
        val date: ActionDate = ActionDate.NOW
    ) : ShowDetailsAction()

    data class MarkSeasonUnwatchedAction(val seasonId: Long) : ShowDetailsAction()
    data class ChangeSeasonFollowedAction(
        val seasonId: Long,
        val followed: Boolean
    ) : ShowDetailsAction()

    data class UnfollowPreviousSeasonsFollowedAction(val seasonId: Long) : ShowDetailsAction()

    data class ChangeSeasonExpandedAction(
        val seasonId: Long,
        val expanded: Boolean
    ) : ShowDetailsAction()

    data class OpenShowDetails(val showId: Long) : ShowDetailsAction()
    data class OpenEpisodeDetails(val episodeId: Long) : ShowDetailsAction()
    object ClearError : ShowDetailsAction()
    object NavigateUp : ShowDetailsAction()
}
