// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

enum class Request(val tag: String) {
    SHOW_DETAILS("show_details"),
    SHOW_IMAGES("show_images"),
    SHOW_SEASONS("show_seasons"),
    EPISODE_DETAILS("episode_details"),
    SHOW_EPISODE_WATCHES("show_episode_watches"),
    FOLLOWED_SHOWS("followed_shows"),
    WATCHED_SHOWS("watched_shows"),
    USER_PROFILE("user_profile"),
    RELATED_SHOWS("related_shows"),
    TRENDING_SHOWS("trending_shows"),
    POPULAR_SHOWS("popular_shows"),
    RECOMMENDED_SHOWS("recommended_shows"),
}
