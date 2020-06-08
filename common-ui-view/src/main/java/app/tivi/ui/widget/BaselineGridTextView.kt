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

package app.tivi.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import app.tivi.common.ui.R

/**
 * An extension to [AppCompatTextView] which aligns text to a 4dp baseline grid.
 *
 *
 * To achieve this we expose a `lineHeightHint` allowing you to specify the desired line
 * height (alternatively a `lineHeightMultiplierHint` to use a multiplier of the text size).
 * This line height will be adjusted to be a multiple of 4dp to ensure that baselines sit on
 * the grid.
 *
 *
 * We also adjust spacing above and below the text to ensure that the first line's baseline sits on
 * the grid (relative to the view's top) & that this view's height is a multiple of 4dp so that
 * subsequent views start on the grid.
 */
class BaselineGridTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var lineHeightMultiplierHint = 1f
        set(value) {
            if (field != value) {
                field = value
                computeLineHeight()
            }
        }

    var lineHeightHint = 0
        set(value) {
            if (field != value) {
                field = value
                computeLineHeight()
            }
        }

    var maxLinesByHeight = true
        set(value) {
            if (field != value) {
                field = value
                computeLineHeight()
            }
        }

    private var extraTopPadding = 0
    private var extraBottomPadding = 0
    private val fourDipInPx: Float

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.BaselineGridTextView, defStyleAttr, 0
        )

        // first check TextAppearance for line height & font attributes
        if (a.hasValue(R.styleable.BaselineGridTextView_android_textAppearance)) {
            val textAppearanceId = a.getResourceId(
                R.styleable.BaselineGridTextView_android_textAppearance, android.R.style.TextAppearance
            )
            val ta = context.obtainStyledAttributes(
                textAppearanceId, R.styleable.BaselineGridTextView
            )
            parseTextAttrs(ta)
            ta.recycle()
        }

        // then check view attrs
        parseTextAttrs(a)
        maxLinesByHeight = a.getBoolean(R.styleable.BaselineGridTextView_maxLinesByHeight, false)
        a.recycle()

        fourDipInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)
        computeLineHeight()
    }

    override fun getCompoundPaddingTop(): Int {
        // include extra padding to place the first line's baseline on the grid
        return super.getCompoundPaddingTop() + extraTopPadding
    }

    override fun getCompoundPaddingBottom(): Int {
        // include extra padding to make the height a multiple of 4dp
        return super.getCompoundPaddingBottom() + extraBottomPadding
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        extraTopPadding = 0
        extraBottomPadding = 0

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var height = measuredHeight
        height += ensureBaselineOnGrid()
        height += ensureHeightGridAligned(height)
        setMeasuredDimension(measuredWidth, height)
        checkMaxLines(height, View.MeasureSpec.getMode(heightMeasureSpec))
    }

    private fun parseTextAttrs(a: TypedArray) {
        if (a.hasValue(R.styleable.BaselineGridTextView_lineHeightMultiplierHint)) {
            lineHeightMultiplierHint = a.getFloat(R.styleable.BaselineGridTextView_lineHeightMultiplierHint, 1f)
        }
        if (a.hasValue(R.styleable.BaselineGridTextView_lineHeightHint)) {
            lineHeightHint = a.getDimensionPixelSize(
                R.styleable.BaselineGridTextView_lineHeightHint, 0
            )
        }
    }

    /**
     * Ensures line height is a multiple of 4dp.
     */
    private fun computeLineHeight() {
        val fm = paint.fontMetrics
        val fontHeight = Math.abs(fm.ascent - fm.descent) + fm.leading
        val desiredLineHeight = if (lineHeightHint > 0) lineHeightHint.toFloat() else lineHeightMultiplierHint * fontHeight

        val baselineAlignedLineHeight = Math.round(fourDipInPx * Math.ceil((desiredLineHeight / fourDipInPx).toDouble()))
        setLineSpacing(baselineAlignedLineHeight - fontHeight, 1f)
    }

    /**
     * Ensure that the first line of text sits on the 4dp grid.
     */
    private fun ensureBaselineOnGrid(): Int {
        val baseline = baseline.toFloat()
        val gridAlign = baseline % fourDipInPx
        if (gridAlign != 0f) {
            extraTopPadding = (fourDipInPx - Math.ceil(gridAlign.toDouble())).toInt()
        }
        return extraTopPadding
    }

    /**
     * Ensure that height is a multiple of 4dp.
     */
    private fun ensureHeightGridAligned(height: Int): Int {
        val gridOverhang = height % fourDipInPx
        if (gridOverhang != 0f) {
            extraBottomPadding = (fourDipInPx - Math.ceil(gridOverhang.toDouble())).toInt()
        }
        return extraBottomPadding
    }

    /**
     * When measured with an exact height, text can be vertically clipped mid-line. Prevent
     * this by setting the `maxLines` property based on the available space.
     */
    private fun checkMaxLines(height: Int, heightMode: Int) {
        if (!maxLinesByHeight || heightMode != View.MeasureSpec.EXACTLY) return

        val textHeight = height - compoundPaddingTop - compoundPaddingBottom
        val completeLines = Math.floor((textHeight / lineHeight).toDouble()).toInt()
        maxLines = completeLines
    }
}
