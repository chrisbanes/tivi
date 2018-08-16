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

package app.tivi.ui.widget

import android.content.Context
import android.graphics.Color
import android.support.design.animation.AnimationUtils
import android.support.design.shape.CutCornerTreatment
import android.support.design.shape.MaterialShapeDrawable
import android.support.design.shape.ShapePathModel
import android.util.AttributeSet
import android.view.View
import app.tivi.R

class TopLeftCutoutBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyleAttr: Int = 0
) : View(context, attrs, defaultStyleAttr) {

    private val shapeDrawable: MaterialShapeDrawable = MaterialShapeDrawable().apply {
        shadowElevation = resources.getDimensionPixelSize(R.dimen.details_card_elevation)
        isShadowEnabled = true
    }

    var color: Int = Color.MAGENTA
        set(value) {
            shapeDrawable.setTint(value)
            field = value
        }

    var cutSize: Float = 0f
        set(value) {
            field = value
            syncCutSize()
        }

    var progress: Float = 1f
        set(value) {
            if (value != field) {
                field = value
                syncCutSize()
            }
        }

    private fun syncCutSize() {
        val cutSize = cutSize
        val progress = progress

        val shapeModel = shapeDrawable.shapedViewModel ?: ShapePathModel()

        shapeModel.topLeftCorner = CutCornerTreatment(AnimationUtils.lerp(cutSize, 0f, 1f - progress))

        shapeDrawable.shapedViewModel = shapeModel
    }

    init {
        background = shapeDrawable
        syncCutSize()
    }
}