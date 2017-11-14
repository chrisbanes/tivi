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

package me.banes.chris.tivi.ui.holders

import android.graphics.drawable.Drawable
import android.view.View
import kotlinx.android.synthetic.main.grid_item.*
import me.banes.chris.tivi.extensions.loadFromUrl

class PosterGridHolder(itemView: View) : TiviViewHolder(itemView) {

    fun bindPlaceholder() {
        show_title.visibility = View.GONE
        show_poster.setImageDrawable(null)
    }

    fun bindShow(posterPath: String?,
            title: String?,
            transitionName: String? = null,
            annotation: String? = null,
            annotationDrawable: Drawable? = null) {
        show_title.text = title
        show_title.visibility = View.VISIBLE

        show_poster.setImageDrawable(null)
        if (posterPath != null) {
            show_poster.loadFromUrl("https://image.tmdb.org/t/p/w342$posterPath")
        } else {
            show_title.visibility = View.VISIBLE
        }

        if (annotation != null) {
            show_annotation.text = annotation
            show_annotation.visibility = View.VISIBLE
            show_annotation.setCompoundDrawablesRelativeWithIntrinsicBounds(annotationDrawable, null, null, null)
        } else {
            show_annotation.visibility = View.GONE
        }

        itemView.transitionName = transitionName
    }
}