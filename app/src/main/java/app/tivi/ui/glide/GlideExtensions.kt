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

package app.tivi.ui.glide

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.annotation.GlideExtension
import com.bumptech.glide.annotation.GlideOption
import com.bumptech.glide.annotation.GlideType
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.BaseRequestOptions

@SuppressLint("CheckResult")
@GlideExtension
object GlideExtensions {
    @GlideOption
    @JvmStatic
    fun round(options: BaseRequestOptions<*>, size: Int): BaseRequestOptions<*> {
        return options.circleCrop().override(size)
    }

    @JvmStatic
    @GlideType(Drawable::class)
    fun saturateOnLoad(requestBuilder: RequestBuilder<Drawable>): RequestBuilder<Drawable> {
        return requestBuilder.transition(DrawableTransitionOptions.with(SaturationTransitionFactory()))
    }
}