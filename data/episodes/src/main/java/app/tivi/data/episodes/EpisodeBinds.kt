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

package app.tivi.data.episodes

import me.tatarka.inject.annotations.Provides

interface EpisodeBinds {
    val TraktEpisodeDataSourceImpl.bind: TraktEpisodeDataSource
        @Provides get() = this

    val TmdbEpisodeDataSourceImpl.bind: TmdbEpisodeDataSource
        @Provides get() = this

    val TraktSeasonsEpisodesDataSource.bind: SeasonsEpisodesDataSource
        @Provides get() = this
}

typealias TmdbEpisodeDataSource = EpisodeDataSource
typealias TraktEpisodeDataSource = EpisodeDataSource
