/*
 * Copyright 2018 Google, Inc.
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

import android.graphics.drawable.Drawable
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

fun MenuItem.asGlideTarget(toolbar: Toolbar): Target<Drawable> = object : Target<Drawable> {
    private var request: Request? = null

    override fun onLoadStarted(placeholder: Drawable?) {
        icon = placeholder
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        icon = errorDrawable
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        icon = placeholder
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        icon = resource
    }

    override fun getSize(cb: SizeReadyCallback) {
        cb.onSizeReady(toolbar.height, toolbar.height)
    }

    override fun removeCallback(cb: SizeReadyCallback) {
    }

    override fun getRequest(): Request? {
        return request
    }

    override fun setRequest(request: Request?) {
        this.request = request
    }

    override fun onStop() {
    }

    override fun onStart() {
    }

    override fun onDestroy() {
    }
}