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

package app.tivi.common.imageloading

import android.content.Context
import android.view.MenuItem
import app.tivi.extensions.resolveThemeDimensionPixelSize
import coil.Coil
import coil.request.ImageRequest
import coil.size.PixelSize
import coil.size.Size
import coil.size.SizeResolver
import coil.transform.CircleCropTransformation

fun MenuItem.loadImageUrl(context: Context, url: String, circleCrop: Boolean = true) {
    val request = ImageRequest.Builder(context)
        .data(url)
        .size(object : SizeResolver {
            override suspend fun size(): Size {
                val height = context.resolveThemeDimensionPixelSize(android.R.attr.actionBarSize)
                return PixelSize(height, height)
            }
        })
        .target { icon = it }
        .apply {
            if (circleCrop) {
                transformations(CircleCropTransformation())
            }
        }
        .build()

    Coil.enqueue(request)
}
