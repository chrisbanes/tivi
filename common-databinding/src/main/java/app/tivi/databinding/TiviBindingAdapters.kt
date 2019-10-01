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

package app.tivi.databinding

import android.content.res.Resources
import android.graphics.Outline
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import app.tivi.extensions.doOnApplyWindowInsets
import app.tivi.extensions.requestApplyInsetsWhenAttached
import app.tivi.extensions.resolveThemeReferenceResId
import app.tivi.ui.MaxLinesToggleClickListener
import app.tivi.ui.NoopApplyWindowInsetsListener
import app.tivi.ui.ScrimUtil
import kotlin.math.roundToInt

@BindingAdapter("visibleIfNotNull")
fun visibleIfNotNull(view: View, target: Any?) {
    view.isVisible = target != null
}

@BindingAdapter("visible")
fun visible(view: View, value: Boolean) {
    view.isVisible = value
}

@BindingAdapter("textOrGoneIfEmpty")
fun textOrGoneIfEmpty(view: TextView, s: CharSequence?) {
    view.text = s
    view.isGone = s.isNullOrEmpty()
}

@BindingAdapter("srcRes")
fun imageViewSrcRes(view: ImageView, drawableRes: Int) {
    if (drawableRes != 0) {
        view.setImageResource(drawableRes)
    } else {
        view.setImageDrawable(null)
    }
}

@BindingAdapter("maxLinesToggle")
fun maxLinesClickListener(view: TextView, oldCollapsedMaxLines: Int, newCollapsedMaxLines: Int) {
    if (oldCollapsedMaxLines != newCollapsedMaxLines) {
        // Default to collapsed
        view.maxLines = newCollapsedMaxLines
        // Now set click listener
        view.setOnClickListener(MaxLinesToggleClickListener(newCollapsedMaxLines))
    }
}

@BindingAdapter("backgroundScrim")
fun backgroundScrim(view: View, oldColor: Int, color: Int) {
    if (oldColor != color) {
        view.background = ScrimUtil.makeCubicGradientScrimDrawable(color, 16, Gravity.BOTTOM)
    }
}

@BindingAdapter("foregroundScrim")
fun foregroundScrim(view: View, oldColor: Int, color: Int) {
    if (oldColor != color) {
        view.foreground = ScrimUtil.makeCubicGradientScrimDrawable(color, 16, Gravity.BOTTOM)
    }
}

@BindingAdapter("topCornerOutlineProvider")
fun topCornerOutlineProvider(view: View, oldRadius: Float, radius: Float) {
    view.clipToOutline = true
    if (oldRadius != radius) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height + radius.roundToInt(), radius)
            }
        }
    }
}

@BindingAdapter("roundedCornerOutlineProvider")
fun roundedCornerOutlineProvider(view: View, oldRadius: Float, radius: Float) {
    view.clipToOutline = true
    if (oldRadius != radius) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
    }
}

@BindingAdapter("textAppearanceAttr")
fun textAppearanceAttr(view: TextView, oldTextAppearanceStyleAttr: Int, textAppearanceStyleAttr: Int) {
    if (oldTextAppearanceStyleAttr != textAppearanceStyleAttr) {
        view.setTextAppearance(view.context.resolveThemeReferenceResId(textAppearanceStyleAttr))
    }
}

@BindingAdapter("fontFamily")
fun fontFamily(view: TextView, oldFontFamily: Int, fontFamily: Int) {
    if (oldFontFamily != fontFamily) {
        view.typeface = try {
            ResourcesCompat.getFont(view.context, fontFamily)
        } catch (nfe: Resources.NotFoundException) {
            null
        } ?: Typeface.DEFAULT
    }
}

@BindingAdapter("noopInsets")
fun noopApplyWindowInsets(view: View, enabled: Boolean) {
    if (enabled) {
        view.setOnApplyWindowInsetsListener(NoopApplyWindowInsetsListener)
        view.requestApplyInsetsWhenAttached()
    } else {
        view.setOnApplyWindowInsetsListener(null)
    }
}

@BindingAdapter(
        "paddingLeftSystemWindowInsets",
        "paddingTopSystemWindowInsets",
        "paddingRightSystemWindowInsets",
        "paddingBottomSystemWindowInsets",
        "paddingLeftGestureInsets",
        "paddingTopGestureInsets",
        "paddingRightGestureInsets",
        "paddingBottomGestureInsets",
        "marginLeftSystemWindowInsets",
        "marginTopSystemWindowInsets",
        "marginRightSystemWindowInsets",
        "marginBottomSystemWindowInsets",
        "marginLeftGestureInsets",
        "marginTopGestureInsets",
        "marginRightGestureInsets",
        "marginBottomGestureInsets",
        requireAll = false
)
fun applySystemWindows(
    view: View,
    padSystemWindowLeft: Boolean,
    padSystemWindowTop: Boolean,
    padSystemWindowRight: Boolean,
    padSystemWindowBottom: Boolean,
    padGestureLeft: Boolean,
    padGestureTop: Boolean,
    padGestureRight: Boolean,
    padGestureBottom: Boolean,
    marginSystemWindowLeft: Boolean,
    marginSystemWindowTop: Boolean,
    marginSystemWindowRight: Boolean,
    marginSystemWindowBottom: Boolean,
    marginGestureLeft: Boolean,
    marginGestureTop: Boolean,
    marginGestureRight: Boolean,
    marginGestureBottom: Boolean
) {
    require(((padSystemWindowLeft && padGestureLeft) ||
            (padSystemWindowTop && padGestureTop) ||
            (padSystemWindowRight && padGestureRight) ||
            (padSystemWindowBottom && padGestureBottom) ||
            (marginSystemWindowLeft && marginGestureLeft) ||
            (marginSystemWindowTop && marginGestureTop) ||
            (marginSystemWindowRight && marginGestureRight) ||
            (marginSystemWindowBottom && marginGestureBottom)).not()) {
        "Invalid parameters. Can not request system window and gesture inset handling" +
                " for the same dimension"
    }

    view.doOnApplyWindowInsets { v, insets, initialPadding, initialMargin ->
        // Padding handling
        val paddingLeft = when {
            padGestureLeft -> insets.systemGestureInsets.left
            padSystemWindowLeft -> insets.systemWindowInsetLeft
            else -> 0
        }
        val paddingTop = when {
            padGestureTop -> insets.systemGestureInsets.top
            padSystemWindowTop -> insets.systemWindowInsetTop
            else -> 0
        }
        val paddingRight = when {
            padGestureRight -> insets.systemGestureInsets.right
            padSystemWindowRight -> insets.systemWindowInsetRight
            else -> 0
        }
        val paddingBottom = when {
            padGestureBottom -> insets.systemGestureInsets.bottom
            padSystemWindowBottom -> insets.systemWindowInsetBottom
            else -> 0
        }
        v.setPadding(
                initialPadding.left + paddingLeft,
                initialPadding.top + paddingTop,
                initialPadding.right + paddingRight,
                initialPadding.bottom + paddingBottom
        )

        // Margin handling
        val marginInsetRequested = marginSystemWindowLeft || marginGestureLeft ||
                marginSystemWindowTop || marginGestureTop || marginSystemWindowRight ||
                marginGestureRight || marginSystemWindowBottom || marginGestureBottom
        if (marginInsetRequested) {
            require(v.layoutParams is ViewGroup.MarginLayoutParams) {
                "Margin inset handling requested but view LayoutParams do not" +
                        " extend MarginLayoutParams"
            }

            val marginLeft = when {
                marginGestureLeft -> insets.systemGestureInsets.left
                marginSystemWindowLeft -> insets.systemWindowInsetLeft
                else -> 0
            }
            val marginTop = when {
                marginGestureTop -> insets.systemGestureInsets.top
                marginSystemWindowTop -> insets.systemWindowInsetTop
                else -> 0
            }
            val marginRight = when {
                marginGestureRight -> insets.systemGestureInsets.right
                marginSystemWindowRight -> insets.systemWindowInsetRight
                else -> 0
            }
            val marginBottom = when {
                marginGestureBottom -> insets.systemGestureInsets.bottom
                marginSystemWindowBottom -> insets.systemWindowInsetBottom
                else -> 0
            }
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = initialMargin.left + marginLeft
                topMargin = initialMargin.top + marginTop
                rightMargin = initialMargin.right + marginRight
                bottomMargin = initialMargin.bottom + marginBottom
            }
        }
    }
}
