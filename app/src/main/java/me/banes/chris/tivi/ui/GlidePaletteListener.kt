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

package me.banes.chris.tivi.ui

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class GlidePaletteListener(private val listener: (Palette) -> Unit) : RequestListener<Drawable> {
    override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<Drawable>,
            isFirstResource: Boolean): Boolean {
        return false
    }

    override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable>,
            dataSource: DataSource,
            isFirstResource: Boolean): Boolean {
        if (resource is BitmapDrawable) {
            val bitmap = resource.bitmap
            Palette.Builder(bitmap)
                    .clearTargets()
                    .maximumColorCount(4)
                    .setRegion(0, Math.round(bitmap.height * 0.9f), bitmap.width, bitmap.height)
                    .generate {
                        listener(it)
                    }
        }

        // We don't want to handle updating the target
        return false
    }
}
