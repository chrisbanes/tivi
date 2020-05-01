/*
 * Copyright 2020 Google LLC
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

package app.tivi.common.compose

import android.content.res.TypedArray
import android.util.TypedValue
import androidx.core.content.res.getColorOrThrow
import androidx.ui.foundation.shape.corner.CornerSize
import androidx.ui.graphics.Color
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.asFontFamily
import androidx.ui.text.font.font
import androidx.ui.unit.Density
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import androidx.ui.unit.em
import androidx.ui.unit.px
import androidx.ui.unit.sp
import kotlin.concurrent.getOrSet

private val tempTypedValue = ThreadLocal<TypedValue>()

fun TypedArray.getComposeColor(
    index: Int,
    fallbackColor: Color = Color.Unset
): Color = if (hasValue(index)) Color(getColorOrThrow(index)) else fallbackColor

fun TypedArray.getFontFamily(index: Int, fallback: FontFamily): FontFamily {
    return getFontFamilyOrNull(index) ?: fallback
}

fun TypedArray.getFontFamilyOrNull(index: Int): FontFamily? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv) && tv.type == TypedValue.TYPE_STRING) {
        return font(tv.resourceId).asFontFamily()
    }
    return null
}

fun TypedArray.getTextUnit(
    density: Density,
    index: Int,
    fallback: TextUnit = TextUnit.Inherit
): TextUnit = getTextUnitOrNull(density, index) ?: fallback

fun TypedArray.getTextUnitOrNull(
    density: Density,
    index: Int
): TextUnit? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv) && tv.type == TypedValue.TYPE_DIMENSION) {
        return when (tv.complexUnit) {
            // For SP values, we convert the value directly to an TextUnit.Sp
            TypedValue.COMPLEX_UNIT_SP -> TypedValue.complexToFloat(tv.data).sp
            // For DIP values, we convert the value to an TextUnit.Em (roughly equivalent)
            TypedValue.COMPLEX_UNIT_DIP -> TypedValue.complexToFloat(tv.data).em
            // For another other types, we let the TypedArray flatten to a px value, and
            // we convert it to an Sp based on the current density
            else -> with(density) { getDimension(index, 0f).toSp() }
        }
    }
    return null
}

fun TypedArray.getCornerSizeOrNull(index: Int): CornerSize? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv)) {
        return when (tv.type) {
            TypedValue.TYPE_DIMENSION -> {
                when (tv.complexUnit) {
                    // For DIP and PX values, we convert the value to the equivalent
                    TypedValue.COMPLEX_UNIT_DIP -> CornerSize(TypedValue.complexToFloat(tv.data).dp)
                    TypedValue.COMPLEX_UNIT_PX -> CornerSize(TypedValue.complexToFloat(tv.data).px)
                    // For another other dim types, we let the TypedArray flatten to a px value
                    else -> CornerSize(getDimensionPixelSize(index, 0).px)
                }
            }
            TypedValue.TYPE_FRACTION -> CornerSize(tv.getFraction(1f, 1f))
            else -> null
        }
    }
    return null
}

fun TypedArray.getCornerSize(index: Int, fallback: CornerSize): CornerSize {
    return getCornerSizeOrNull(index) ?: fallback
}
