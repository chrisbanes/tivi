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

package app.tivi.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Keep
import app.tivi.common.ui.R
import app.tivi.ui.animations.lerp
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

class TopLeftCutoutBackgroundView : View {
    private val shapeDrawable = MaterialShapeDrawable()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TopLeftCutoutBackgroundView)
        color = a.getColor(R.styleable.TopLeftCutoutBackgroundView_backgroundColor, Color.MAGENTA)
        maxCutSize = a.getDimension(R.styleable.TopLeftCutoutBackgroundView_topLeftCutSize, 0f)
        a.recycle()

        background = shapeDrawable
        syncCutSize()
    }

    @set:Keep
    var color: Int = Color.MAGENTA
        set(value) {
            shapeDrawable.setTint(value)
            field = value
        }

    var maxCutSize: Float = 0f
        set(value) {
            if (value != field) {
                field = value
                syncCutSize()
            }
        }

    @set:Keep
    var cutProgress: Float
        get() = shapeDrawable.interpolation
        set(value) {
            shapeDrawable.interpolation = value
        }

    private fun syncCutSize() {
        shapeDrawable.shapeAppearanceModel = shapeDrawable.shapeAppearanceModel.toBuilder()
            .setTopLeftCorner(CornerFamily.CUT, lerp(0f, maxCutSize, cutProgress))
            .build()
    }
}
