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

package me.banes.chris.tivi.details.items

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.details_summary_item.view.*
import kotlinx.android.synthetic.main.details_title_poster_item.view.*
import kotlinx.android.synthetic.main.fragment_show_details.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.doWhenLaidOut
import me.banes.chris.tivi.extensions.loadFromUrl
import me.banes.chris.tivi.tmdb.TmdbImageSizes
import me.banes.chris.tivi.tmdb.TmdbImageUrlProvider
import me.banes.chris.tivi.ui.groupieitems.TiviItem
import me.banes.chris.tivi.ui.holders.TiviViewHolder

class TitleItem(private val show: TiviShow) : TiviItem<TiviViewHolder>() {

    override fun getLayout() = R.layout.details_title_poster_item

    override fun bind(viewHolder: TiviViewHolder, position: Int) {
        viewHolder.itemView.details_title.text = show.title

        if (show.originalTitle != null) {
            viewHolder.itemView.details_subtitle.apply {
                text = show.originalTitle
                visibility = View.VISIBLE
            }
        } else {
            viewHolder.itemView.details_subtitle.visibility = View.GONE
        }

        if (show.tmdbPosterPath != null) {
            val imageUrlProvider = TmdbImageUrlProvider(TmdbImageSizes)
            viewHolder.itemView.details_show_poster.doWhenLaidOut {
                viewHolder.itemView.details_show_poster.loadFromUrl(
                        imageUrlProvider.getPosterUrl(show.tmdbPosterPath!!, viewHolder.itemView.details_show_poster.width))
            }
        }
    }

    override fun isClickable() = false
}