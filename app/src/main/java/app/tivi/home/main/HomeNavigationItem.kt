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

enum class HomeNavigationItem(val labelResId: Int, val iconResId: Int) {
    DISCOVER(R.string.discover_title, R.drawable.ic_eye_24dp),
    WATCHED(R.string.library_watched, R.drawable.ic_eye_24dp),
    FOLLOWED(R.string.library_followed_shows, R.drawable.ic_eye_24dp)
}