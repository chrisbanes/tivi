/*
 * Copyright 2017 Google LLC
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

package app.tivi.data.models

/* ktlint-disable trailing-comma-on-declaration-site */
enum class Genre(val traktValue: String) {
    DRAMA("drama"),
    FANTASY("fantasy"),
    SCIENCE_FICTION("science-fiction"),
    ACTION("action"),
    ADVENTURE("adventure"),
    CRIME("crime"),
    THRILLER("thriller"),
    COMEDY("comedy"),
    HORROR("horror"),
    MYSTERY("mystery");

    companion object {
        private val values by lazy { values() }
        fun fromTraktValue(value: String) = values.firstOrNull { it.traktValue == value }
    }
}
