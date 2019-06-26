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

package app.tivi.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.res.use
import app.tivi.R
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

class MaterialShapeConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : CheckableConstraintLayout(context, attrs, defStyle) {

    init {
        context.obtainStyledAttributes(attrs, R.styleable.MaterialShapeConstraintLayout).use { ta ->
            background = MaterialShapeDrawable.createWithElevationOverlay(context, elevation).apply {
                fillColor = ta.getColorStateList(R.styleable.MaterialShapeConstraintLayout_materialBackgroundColor)

                val topLeft = ta.getDimensionPixelSize(
                        R.styleable.MaterialShapeConstraintLayout_materialBackgroundTopLeftRadius, 0)
                if (topLeft > 0) {
                    shapeAppearanceModel.setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
                }
                val topRight = ta.getDimensionPixelSize(
                        R.styleable.MaterialShapeConstraintLayout_materialBackgroundTopRightRadius, 0)
                if (topRight > 0) {
                    shapeAppearanceModel.setTopRightCorner(CornerFamily.ROUNDED, topRight)
                }
            }
        }
    }

    override fun setBackground(background: Drawable?) {
        super.setBackground(background)

        if (background is MaterialShapeDrawable) {
            background.elevation = elevation
            background.translationZ = translationZ
        }
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)

        val bg = background
        if (bg is MaterialShapeDrawable) {
            bg.elevation = elevation
        }
    }

    override fun setTranslationZ(translationZ: Float) {
        super.setTranslationZ(translationZ)

        val bg = background
        if (bg is MaterialShapeDrawable) {
            bg.translationZ = translationZ
        }
    }
}