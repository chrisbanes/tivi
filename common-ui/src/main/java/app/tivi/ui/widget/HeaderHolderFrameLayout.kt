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
import android.widget.FrameLayout
import androidx.core.content.res.use
import app.tivi.common.ui.R

class HeaderHolderFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    var dividerDrawable: Drawable? = null
        set(value) {
            field?.callback = null
            if (value != null) {
                value.callback = this
            }
            field = value
        }

    var dividerMarginEnd: Int = 0
        set(value) {
            field = value
            dividerDrawable?.run { invalidateSelf() }
        }

    var dividerMarginStart: Int = 0
        set(value) {
            field = value
            dividerDrawable?.run { invalidateSelf() }
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.HeaderHolderFrameLayout, defStyle, 0).use {
            dividerDrawable = it.getDrawable(R.styleable.HeaderHolderFrameLayout_dividerDrawable)
            dividerMarginStart = it.getDimensionPixelSize(
                    R.styleable.HeaderHolderFrameLayout_dividerMarginStart, 0)
            dividerMarginEnd = it.getDimensionPixelSize(
                    R.styleable.HeaderHolderFrameLayout_dividerMarginEnd, 0)
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        dividerDrawable?.run {
            val left = when (layoutDirection) {
                View.LAYOUT_DIRECTION_RTL -> dividerMarginEnd
                else -> dividerMarginStart
            }
            val right = when (layoutDirection) {
                View.LAYOUT_DIRECTION_RTL -> dividerMarginStart
                else -> dividerMarginEnd
            }
            setBounds(left, height - intrinsicHeight, width - right, height)
        }
        dividerDrawable?.draw(canvas)
    }
}