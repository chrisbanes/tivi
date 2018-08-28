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

package app.tivi.ui.epoxy

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.view.View
import app.tivi.ui.animations.lerp
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyTouchHelper
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

abstract class SwipeAwayCallbacks<T : EpoxyModel<*>>(
    private val icon: Drawable,
    private val padding: Int,
    @ColorInt private val backgroundColor: Int,
    @ColorInt private val accentColor: Int
) : EpoxyTouchHelper.SwipeCallbacks<T>() {
    private val rect = RectF()
    private val iconBounds = Rect()

    private val iconHeight = icon.intrinsicHeight
    private val iconWidth = icon.intrinsicWidth

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
    }

    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentColor
    }

    override fun onSwipeStarted(
        model: T,
        itemView: View?,
        adapterPosition: Int
    ) {
        super.onSwipeStarted(model, itemView, adapterPosition)
    }

    override fun onSwipeProgressChanged(
        model: T,
        itemView: View,
        swipeProgress: Float,
        canvas: Canvas
    ) {
        rect.set(itemView.left.toFloat(), itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
        rect.offset(itemView.translationX, itemView.translationY)

        var radius = 0f

        val save = canvas.save()

        if (rect.left > 0) {
            // Swiping left-to-right
            rect.right = rect.left
            rect.left = 0f

            canvas.clipRect(rect)

            val left = rect.left.roundToInt() + padding
            val top = (rect.top + (rect.height() - iconHeight) / 2).roundToInt()
            iconBounds.set(left, top, left + iconWidth, top + iconHeight)
            icon.bounds = iconBounds

            val startValue = iconBounds.right
            val endValue = rect.left + itemView.width / 3
            val fraction = ((rect.right - startValue) / (endValue - startValue)).coerceIn(0f, 1f)

            val maxRadius = Math.hypot(rect.right.toDouble() - iconBounds.centerX(), iconBounds.centerY().toDouble())
            radius = lerp(0f, maxRadius.toFloat(), fraction)

        } else if (rect.right < canvas.width) {
            // Swiping right-to-left
            rect.left = rect.right
            rect.right = canvas.width.toFloat()

            canvas.clipRect(rect)

            val right = rect.right.roundToInt() - padding
            val top = (rect.top + (rect.height() - iconHeight) / 2).roundToInt()
            iconBounds.set(right - iconWidth, top, right, top + iconHeight)
            icon.bounds = iconBounds
        }

        // Draw the background color
        canvas.drawRect(rect, backgroundPaint)

        if (radius > 0) {
            canvas.drawCircle(iconBounds.centerX().toFloat(), iconBounds.centerY().toFloat(), radius, foregroundPaint)
        }

        // Now draw the icon
        icon.draw(canvas)

        canvas.restoreToCount(save)
    }
}