/*
 * Copyright 2017 Google LLC
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

package app.tivi.ui.transitions

import android.graphics.drawable.Drawable
import android.util.Property

object DrawableAlphaProperty : Property<Drawable, Int>(Int::class.java, "drawableAlpha") {
    override fun set(d: Drawable, value: Int) {
        d.alpha = value
    }

    override fun get(d: Drawable): Int = d.alpha
}
