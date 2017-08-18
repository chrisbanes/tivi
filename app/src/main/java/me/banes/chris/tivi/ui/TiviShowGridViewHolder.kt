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
 *
 */

package me.banes.chris.tivi.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.TiviShow
import loadFromUrl

class TiviShowGridViewHolder(itemView: View) : ViewHolder(itemView) {

    private val title = itemView.findViewById<TextView>(R.id.show_title)
    private val poster = itemView.findViewById<ImageView>(R.id.show_poster)

    fun bindShow(item: TiviShow) {
        title.text = item.title
        title.visibility = View.VISIBLE

        poster.setImageDrawable(null)
        if (item.tmdbPosterPath != null) {
            poster.loadFromUrl("https://image.tmdb.org/t/p/w342${item.tmdbPosterPath}")
        } else {
            title.visibility = View.VISIBLE
        }
    }
}