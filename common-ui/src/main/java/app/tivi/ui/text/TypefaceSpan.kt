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

package app.tivi.ui.text

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class TypefaceSpan(private val typeface: Typeface? = null) : MetricAffectingSpan() {

    override fun updateMeasureState(p: TextPaint) {
        if (typeface != null) {
            p.typeface = typeface
            // Note: This flag is required for proper typeface rendering
            p.flags = p.flags or Paint.SUBPIXEL_TEXT_FLAG
        }
    }

    override fun updateDrawState(tp: TextPaint) {
        if (typeface != null) {
            tp.typeface = typeface
            // Note: This flag is required for proper typeface rendering
            tp.flags = tp.flags or Paint.SUBPIXEL_TEXT_FLAG
        }
    }
}
