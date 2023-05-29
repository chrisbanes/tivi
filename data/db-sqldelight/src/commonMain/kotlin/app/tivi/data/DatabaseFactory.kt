// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import app.cash.sqldelight.adapter.primitive.FloatColumnAdapter
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.tivi.data.columnadaptors.DayOfWeekColumnAdapter
import app.tivi.data.columnadaptors.ImageTypeColumnAdapter
import app.tivi.data.columnadaptors.InstantLongColumnAdapter
import app.tivi.data.columnadaptors.InstantStringColumnAdapter
import app.tivi.data.columnadaptors.LocalTimeColumnAdapter
import app.tivi.data.columnadaptors.PendingActionColumnAdapter
import app.tivi.data.columnadaptors.RequestColumnAdapter
import app.tivi.data.columnadaptors.ShowStatusColumnAdapter
import app.tivi.data.columnadaptors.TimeZoneColumnAdapter
import me.tatarka.inject.annotations.Inject

@Inject
class DatabaseFactory(
    private val driver: SqlDriver,
) {
    fun build(): Database = Database(
        driver = driver,
        episodesAdapter = Episodes.Adapter(
            trakt_idAdapter = IntColumnAdapter,
            tmdb_idAdapter = IntColumnAdapter,
            numberAdapter = IntColumnAdapter,
            first_airedAdapter = InstantStringColumnAdapter,
            trakt_ratingAdapter = FloatColumnAdapter,
            trakt_rating_votesAdapter = IntColumnAdapter,
        ),
        episode_watch_entriesAdapter = Episode_watch_entries.Adapter(
            watched_atAdapter = InstantStringColumnAdapter,
            pending_actionAdapter = PendingActionColumnAdapter,
        ),
        popular_showsAdapter = Popular_shows.Adapter(
            pageAdapter = IntColumnAdapter,
            page_orderAdapter = IntColumnAdapter,
        ),
        last_requestsAdapter = Last_requests.Adapter(
            requestAdapter = RequestColumnAdapter,
            timestampAdapter = InstantLongColumnAdapter,
        ),
        myshows_entriesAdapter = Myshows_entries.Adapter(
            followed_atAdapter = InstantStringColumnAdapter,
            pending_actionAdapter = PendingActionColumnAdapter,
        ),
        recommended_entriesAdapter = Recommended_entries.Adapter(
            pageAdapter = IntColumnAdapter,
        ),
        related_showsAdapter = Related_shows.Adapter(
            order_indexAdapter = IntColumnAdapter,
        ),
        seasonsAdapter = Seasons.Adapter(
            trakt_idAdapter = IntColumnAdapter,
            tmdb_idAdapter = IntColumnAdapter,
            numberAdapter = IntColumnAdapter,
            ep_countAdapter = IntColumnAdapter,
            ep_airedAdapter = IntColumnAdapter,
            trakt_ratingAdapter = FloatColumnAdapter,
            trakt_votesAdapter = IntColumnAdapter,
        ),
        showsAdapter = Shows.Adapter(
            trakt_idAdapter = IntColumnAdapter,
            tmdb_idAdapter = IntColumnAdapter,
            trakt_ratingAdapter = FloatColumnAdapter,
            trakt_votesAdapter = IntColumnAdapter,
            runtimeAdapter = IntColumnAdapter,
            first_airedAdapter = InstantStringColumnAdapter,
            statusAdapter = ShowStatusColumnAdapter,
            airs_dayAdapter = DayOfWeekColumnAdapter,
            airs_timeAdapter = LocalTimeColumnAdapter,
            airs_tzAdapter = TimeZoneColumnAdapter,
        ),
        show_imagesAdapter = Show_images.Adapter(
            typeAdapter = ImageTypeColumnAdapter,
            ratingAdapter = FloatColumnAdapter,
        ),
        trending_showsAdapter = Trending_shows.Adapter(
            pageAdapter = IntColumnAdapter,
            watchersAdapter = IntColumnAdapter,
        ),
        watched_entriesAdapter = Watched_entries.Adapter(
            last_watchedAdapter = InstantStringColumnAdapter,
            last_updatedAdapter = InstantStringColumnAdapter,
        ),
        usersAdapter = Users.Adapter(
            joined_dateAdapter = InstantStringColumnAdapter,
        ),
    )
}
