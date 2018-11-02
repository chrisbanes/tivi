/*
 * Copyright 2018 Google LLC
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

package app.tivi.home.library

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.tivi.R

enum class LibraryFilter(
    @StringRes val labelResource: Int,
    @DrawableRes val iconResource: Int
) {
    FOLLOWED(R.string.library_followed_shows, R.drawable.ic_heart_24dp),
    WATCHED(R.string.library_watched, R.drawable.ic_clock_24dp)
}