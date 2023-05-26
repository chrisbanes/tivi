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

package app.tivi.screens

import com.slack.circuit.runtime.Screen

@CommonParcelize
object AccountScreen : TiviScreen(name = "AccountUi()")

@CommonParcelize
object DiscoverScreen : TiviScreen(name = "Discover()")

@CommonParcelize
data class EpisodeDetailsScreen(val id: Long) : TiviScreen(name = "EpisodeDetails()") {
    override val arguments get() = mapOf("id" to id)
}

@CommonParcelize
data class EpisodeTrackScreen(val id: Long) : TiviScreen(name = "EpisodeTrack()") {
    override val arguments get() = mapOf("id" to id)
}

@CommonParcelize
object LibraryScreen : TiviScreen(name = "Library()")

@CommonParcelize
object PopularShowsScreen : TiviScreen(name = "PopularShows()")

@CommonParcelize
object RecommendedShowsScreen : TiviScreen(name = "RecommendedShows()")

@CommonParcelize
object SearchScreen : TiviScreen(name = "Search()")

@CommonParcelize
object SettingsScreen : TiviScreen(name = "Settings()")

@CommonParcelize
data class ShowDetailsScreen(val id: Long) : TiviScreen(name = "ShowDetails()") {
    override val arguments get() = mapOf("id" to id)
}

@CommonParcelize
data class ShowSeasonsScreen(
    val id: Long,
    val selectedSeasonId: Long? = null,
) : TiviScreen(name = "ShowSeasons()") {
    override val arguments get() = mapOf(
        "id" to id,
        "selectedSeasonId" to selectedSeasonId,
    )
}

@CommonParcelize
object TrendingShowsScreen : TiviScreen(name = "TrendingShows()")

@CommonParcelize
object UpNextScreen : TiviScreen(name = "UpNext()")

abstract class TiviScreen(val name: String) : Screen {
    open val arguments: Map<String, *>? = null
}
