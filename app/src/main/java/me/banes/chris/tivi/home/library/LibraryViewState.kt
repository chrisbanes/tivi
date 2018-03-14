/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.home.library

import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.data.entities.MyShowsEntry
import me.banes.chris.tivi.data.entities.WatchedEntry
import me.banes.chris.tivi.tmdb.TmdbImageUrlProvider

data class LibraryViewState(
    val watched: List<ListItem<WatchedEntry>>,
    val myShows: List<ListItem<MyShowsEntry>>,
    val tmdbImageUrlProvider: TmdbImageUrlProvider
)