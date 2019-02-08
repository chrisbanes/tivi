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

package app.tivi.showdetails.details

import android.content.Context
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.italic
import androidx.emoji.text.EmojiCompat
import app.tivi.R
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Genre
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.inject.PerActivity
import app.tivi.ui.GenreStringer
import app.tivi.ui.text.textAppearanceSpanForAttribute
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

class ShowDetailsTextCreator @Inject constructor(
    @PerActivity private val context: Context
) {
    fun seasonSummaryText(season: SeasonWithEpisodesAndWatches): CharSequence {
        val toWatch = season.numberAiredToWatch
        val toAir = season.numberToAir
        val watched = season.numberWatched

        val text = StringBuilder()
        if (toWatch > 0) {
            text.append(context.getString(R.string.season_summary_to_watch, toWatch))
        }
        if (toAir > 0) {
            if (text.isNotEmpty()) {
                text.append(" \u2022 ")
            }
            text.append(context.getString(R.string.season_summary_to_air, toAir))
        }
        if (watched > 0) {
            if (text.isNotEmpty()) {
                text.append(" \u2022 ")
            }
            text.append(context.getString(R.string.season_summary_watched, watched))
        }
        return text
    }

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

    fun genreString(genres: List<Genre>?): CharSequence? {
        if (genres != null && genres.isNotEmpty()) {
            val spanned = buildSpannedString {
                for (i in 0 until genres.size) {
                    val genre = genres[i]
                    inSpans(textAppearanceSpanForAttribute(context, R.attr.textAppearanceCaption)) {
                        append(context.getString(GenreStringer.getLabel(genre)))
                    }
                    append("\u00A0") // nbsp
                    append(GenreStringer.getEmoji(genre))
                    if (i < genres.size - 1) append(" \u2022 ")
                }
            }

            val emojiCompat = EmojiCompat.get()
            return when {
                emojiCompat.loadState == EmojiCompat.LOAD_STATE_SUCCEEDED -> emojiCompat.process(spanned)
                else -> spanned
            }
        }
        return null
    }

    fun genreContentDescription(genres: List<Genre>?): CharSequence? {
        return genres?.joinToString(", ") { context.getString(GenreStringer.getLabel(it)) }
    }
}