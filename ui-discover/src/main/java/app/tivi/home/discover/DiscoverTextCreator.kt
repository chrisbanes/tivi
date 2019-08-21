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
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.inject.PerActivity
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

class DiscoverTextCreator @Inject constructor(
    @PerActivity private val context: Context
) {
    fun episodeTitleText(episode: Episode): CharSequence? {
        val firstAired = episode.firstAired
        val title = episode.title ?: context.getString(R.string.not_known_title)

        if (firstAired == null || firstAired.isAfter(OffsetDateTime.now())) {
            return buildSpannedString {
                italic {
                    append(title)
                }
            }
        }
        return title
    }

    fun seasonEpisodeTitleText(season: Season, episode: Episode): String {
        return context.getString(R.string.season_episode_number, season.number, episode.number)
    }
}