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

package app.tivi.extensions

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.tivi.ui.glide.GlideApp
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

fun ImageView.loadFromUrl(imageUrl: String) {
    GlideApp.with(this)
            .saturateOnLoad()
            .load(imageUrl)
            .into(this)
}

fun ImageView.loadFromUrl(thumbnailUrl: String, imageUrl: String) {
    GlideApp.with(this)
            .saturateOnLoad()
            .load(imageUrl)
            .thumbnail(GlideApp.with(this).load(thumbnailUrl))
            .into(this)
}

fun MenuItem.loadIconFromUrl(context: Context, imageUrl: String) {
    GlideApp.with(context).asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap>(100, 100) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    icon = RoundedBitmapDrawableFactory.create(context.resources, resource).apply {
                        isCircular = true
                    }
                }
            })
}

val View.marginLeft: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0

val View.marginTop: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0

val View.marginRight: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0

val View.marginBottom: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0

val View.marginStart: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart ?: 0

val View.marginEnd: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.marginEnd ?: 0