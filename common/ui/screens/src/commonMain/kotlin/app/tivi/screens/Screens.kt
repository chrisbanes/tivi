// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.screens

import com.slack.circuit.runtime.screen.Screen

@Parcelize
object AccountScreen : TiviScreen(name = "AccountUi()")

@Parcelize
object DiscoverScreen : TiviScreen(name = "Discover()")

@Parcelize
data class EpisodeDetailsScreen(val id: Long) : TiviScreen(name = "EpisodeDetails()") {
  override val arguments get() = mapOf("id" to id)
}

@Parcelize
data class EpisodeTrackScreen(val id: Long) : TiviScreen(name = "EpisodeTrack()") {
  override val arguments get() = mapOf("id" to id)
}

@Parcelize
object LibraryScreen : TiviScreen(name = "Library()")

@Parcelize
object PopularShowsScreen : TiviScreen(name = "PopularShows()")

@Parcelize
object RecommendedShowsScreen : TiviScreen(name = "RecommendedShows()")

@Parcelize
object SearchScreen : TiviScreen(name = "Search()")

@Parcelize
object SettingsScreen : TiviScreen(name = "Settings()")

@Parcelize
object LicensesScreen : TiviScreen(name = "LicensesScreen()")

@Parcelize
object DevSettingsScreen : TiviScreen(name = "DevelopmentSettings()")

@Parcelize
object DevNotificationsScreen : TiviScreen("DevNotificationsScreen()")

@Parcelize
object DevLogScreen : TiviScreen(name = "DevelopmentLog()")

@Parcelize
data class UrlScreen(val url: String) : TiviScreen(name = "UrlScreen()") {
  override val arguments get() = mapOf("url" to url)
}

@Parcelize
data class ShowDetailsScreen(val id: Long) : TiviScreen(name = "ShowDetails()") {
  override val arguments get() = mapOf("id" to id)
}

@Parcelize
data class ShowSeasonsScreen(
  val showId: Long,
  val selectedSeasonId: Long? = null,
  val openEpisodeId: Long? = null,
) : TiviScreen(name = "ShowSeasons()") {
  override val arguments get() = mapOf(
    "showId" to showId,
    "selectedSeasonId" to selectedSeasonId,
  )
}

@Parcelize
object TrendingShowsScreen : TiviScreen(name = "TrendingShows()")

@Parcelize
object UpNextScreen : TiviScreen(name = "UpNext()")

abstract class TiviScreen(val name: String) : Screen {
  open val arguments: Map<String, *>? = null
}
