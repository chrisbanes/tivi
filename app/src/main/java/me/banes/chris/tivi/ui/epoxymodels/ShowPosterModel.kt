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

package me.banes.chris.tivi.ui.epoxymodels

import android.support.annotation.DrawableRes
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.grid_item.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.extensions.doWhenLaidOut
import me.banes.chris.tivi.extensions.loadFromUrl
import me.banes.chris.tivi.tmdb.TmdbImageUrlProvider

@EpoxyModelClass(layout = R.layout.grid_item)
abstract class ShowPosterModel : EpoxyModelWithHolder<TiviEpoxyHolder>() {
    @EpoxyAttribute var title: String? = null
    @EpoxyAttribute var transName: String? = null
    @EpoxyAttribute var posterPath: String? = null
    @EpoxyAttribute var annotation: String? = null
    @EpoxyAttribute @DrawableRes var annotationDrawable = 0
    @EpoxyAttribute var tmdbImageUrlProvider: TmdbImageUrlProvider? = null
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash) var clickListener: View.OnClickListener? = null

    override fun bind(holder: TiviEpoxyHolder) {
        holder.show_title.text = title
        holder.show_title.visibility = View.VISIBLE

        holder.show_poster.setImageDrawable(null)
        if (posterPath != null && tmdbImageUrlProvider != null) {
            holder.show_poster.doWhenLaidOut {
                holder.show_poster.loadFromUrl(tmdbImageUrlProvider!!.getPosterUrl(posterPath!!, it.width))
            }
        } else {
            holder.show_title.visibility = View.VISIBLE
        }

        holder.show_annotation.apply {
            if (annotation != null) {
                text = annotation
                visibility = View.VISIBLE
                setCompoundDrawablesRelativeWithIntrinsicBounds(annotationDrawable, 0, 0, 0)
            } else {
                visibility = View.GONE
            }
        }

        holder.containerView?.apply {
            transitionName = transName
            translationX = 0f
            translationY = 0f
            setOnClickListener(clickListener)
        }
    }
}