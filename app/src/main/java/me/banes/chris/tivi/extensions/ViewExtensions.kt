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

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

fun View.doOnLayout(onLayout: (View) -> Boolean) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            if (onLayout(view)) {
                view.removeOnLayoutChangeListener(this)
            }
        }
    })
}

fun View.updatePadding(paddingStart: Int = getPaddingStart(),
        paddingTop: Int = getPaddingTop(),
        paddingEnd: Int = getPaddingEnd(),
        paddingBottom: Int = getPaddingBottom()) {
    setPaddingRelative(getPaddingStart(), getPaddingTop(), getPaddingEnd(), getPaddingBottom())
}

fun ImageView.loadFromUrl(imageUrl: String) {
    Glide.with(this).load(imageUrl).into(this)
}

fun MenuItem.loadIconFromUrl(context: Context, imageUrl: String) {
    Glide.with(context).asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap>(100, 100) {
                override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                    val circularIcon = RoundedBitmapDrawableFactory.create(context.resources, resource)
                    circularIcon.isCircular = true
                    icon = circularIcon
                }
            })
}