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
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.use
import app.tivi.common.ui.R
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

                val builder = shapeAppearanceModel.toBuilder()

                val topLeft = ta.getDimension(
                    R.styleable.MaterialShapeConstraintLayout_materialBackgroundTopLeftRadius, 0f
                )
                if (topLeft > 0) {
                    builder.setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
                }
                val topRight = ta.getDimension(
                    R.styleable.MaterialShapeConstraintLayout_materialBackgroundTopRightRadius, 0f
                )
                if (topRight > 0) {
                    builder.setTopRightCorner(CornerFamily.ROUNDED, topRight)
                }

                shapeAppearanceModel = builder.build()
            }
        }
    }

    override fun draw(canvas: Canvas?) {
        updateElevationRelativeToParentSurface()
        super.draw(canvas)
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

    private fun updateElevationRelativeToParentSurface() {
        val bg = background
        if (bg is MaterialShapeDrawable) {

            var v = parent
            var cumulativeElevation = elevation

            // Iterate through our parents, until we find a view with a MaterialShapeDrawable
            // background (the 'parent surface'). We then update our background, based on the
            // 'parent surface's elevation
            while (v is View) {
                val vBg = v.background
                if (vBg is MaterialShapeDrawable) {
                    bg.elevation = vBg.elevation.coerceAtLeast(v.elevation) + cumulativeElevation
                    break
                } else {
                    cumulativeElevation += v.elevation
                }

                v = v.getParent()
            }
        }
    }
}
