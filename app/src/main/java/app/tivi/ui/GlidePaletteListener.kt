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

package app.tivi.ui

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette
import android.util.LruCache
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class GlidePaletteListener(private val listener: (Palette) -> Unit) : RequestListener<Drawable> {

    companion object {
        private val cache = LruCache<Any, Palette>(20)
        private val cacheLock = Any()
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Drawable>,
        isFirstResource: Boolean
    ): Boolean = false

    override fun onResourceReady(
        resource: Drawable,
        model: Any,
        target: Target<Drawable>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        // First check the cache
        synchronized(cacheLock) {
            val cached = cache[model]
            if (cached != null) {
                // If the cache has a result now, use it
                listener(cached)
                // We don't want to handle updating the target
                return false
            }
        }

        if (resource is BitmapDrawable) {
            val bitmap = resource.bitmap
            Palette.Builder(bitmap)
                    .clearTargets()
                    .maximumColorCount(4)
                    .setRegion(0, Math.round(bitmap.height * 0.9f), bitmap.width, bitmap.height)
                    .generate { palette ->
                        synchronized(cacheLock) {
                            val cached = cache[model]
                            if (cached != null) {
                                // If the cache has a result now, just return it to maintain equality
                                listener(cached)
                            } else if (palette != null) {
                                // Else we'll save the newly generated one
                                cache.put(model, palette)
                                // Now invoke the listener
                                listener(palette)
                            }
                        }
                    }
        }

        // We don't want to handle updating the target
        return false
    }
}
