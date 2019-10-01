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

package app.tivi.home.main

import app.tivi.R

enum class HomeNavigationItem(val destinationId: Int, val labelResId: Int, val iconResId: Int) {
    DISCOVER(R.id.navigation_discover, R.string.discover_title, R.drawable.ic_popular),
    WATCHED(R.id.navigation_watched, R.string.watched_shows_title, R.drawable.ic_eye_24dp),
    FOLLOWED(R.id.navigation_followed, R.string.following_shows_title, R.drawable.ic_heart_border),
    SETTINGS(R.id.navigation_settings, R.string.settings_title, R.drawable.ic_settings_black_24dp)
}