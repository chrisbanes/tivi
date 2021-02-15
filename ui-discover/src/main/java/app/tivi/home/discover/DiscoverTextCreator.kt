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

package app.tivi.home.discover

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

val LocalDiscoverTextCreator = staticCompositionLocalOf<DiscoverTextCreator> {
    error("DiscoverTextCreator not provided")
}

class DiscoverTextCreator @Inject constructor(
    @ActivityContext private val context: Context
) {
    fun seasonEpisodeTitleText(season: Season, episode: Episode): String {
        return context.getString(R.string.season_episode_number, season.number, episode.number)
    }
}
