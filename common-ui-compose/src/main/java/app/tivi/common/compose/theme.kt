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

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import androidx.annotation.StyleRes
import androidx.compose.Composable
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.core.graphics.ColorUtils
import androidx.ui.core.WithDensity
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.Shadow
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Typography
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.text.font.asFontFamily
import androidx.ui.text.font.font
import androidx.ui.unit.DensityScope
import androidx.ui.unit.TextUnit
import androidx.ui.unit.em
import androidx.ui.unit.px

private const val DEFAULT_COLOR = android.graphics.Color.MAGENTA

/**
 * This component builds upon [androidx.ui.material.MaterialTheme], but reads the
 * Material Design Components Android theme.
 */
@Composable
fun MaterialThemeFromAndroidTheme(
    context: Context,
    children: @Composable() () -> Unit
) = WithDensity {
    context.obtainStyledAttributes(R.styleable.ComposeTheme).use { ta ->
        /* First we'll read the Material color palette */
        val primary = ta.getColor(R.styleable.ComposeTheme_colorPrimary, DEFAULT_COLOR)
        val primaryVariant = ta.getColor(R.styleable.ComposeTheme_colorPrimaryVariant, DEFAULT_COLOR)
        val onPrimary = ta.getColor(R.styleable.ComposeTheme_colorOnPrimary, DEFAULT_COLOR)
        val secondary = ta.getColor(R.styleable.ComposeTheme_colorSecondary, DEFAULT_COLOR)
        val onSecondary = ta.getColor(R.styleable.ComposeTheme_colorOnSecondary, DEFAULT_COLOR)
        val background = ta.getColor(R.styleable.ComposeTheme_android_colorBackground, DEFAULT_COLOR)
        val onBackground = ta.getColor(R.styleable.ComposeTheme_colorOnBackground, DEFAULT_COLOR)
        val surface = ta.getColor(R.styleable.ComposeTheme_colorSurface, DEFAULT_COLOR)
        val onSurface = ta.getColor(R.styleable.ComposeTheme_colorOnSurface, DEFAULT_COLOR)

        val isLightTheme = ColorUtils.calculateLuminance(background) > 0.5f

        val themeColors = if (isLightTheme) {
            lightColorPalette(
                primary = Color(primary),
                primaryVariant = Color(primaryVariant),
                onPrimary = Color(onPrimary),
                secondary = Color(secondary),
                onSecondary = Color(onSecondary),
                background = Color(background),
                onBackground = Color(onBackground),
                surface = Color(surface),
                onSurface = Color(onSurface)
            )
        } else {
            darkColorPalette(
                primary = Color(primary),
                primaryVariant = Color(primaryVariant),
                onPrimary = Color(onPrimary),
                secondary = Color(secondary),
                onSecondary = Color(onSecondary),
                background = Color(background),
                onBackground = Color(onBackground),
                surface = Color(surface),
                onSurface = Color(onSurface)
            )
        }

        /**
         * Next we'll generate a typography instance, using the Material Theme text appearances
         * for TextStyles.
         *
         * We create a normal 'empty' instance first to start from the defaults, then merge in our
         * generated text styles from the Android theme.
         */
        val typography = Typography().merge(
            h1 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline1)),
            h2 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline2)),
            h3 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline3)),
            h4 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline4)),
            h5 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline5)),
            h6 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline6)),
            subtitle1 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceSubtitle1)),
            subtitle2 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceSubtitle2)),
            body1 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceBody1)),
            body2 = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceBody2)),
            button = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceButton)),
            caption = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceCaption)),
            overline = textStyleFromTextAppearance(context, ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceOverline))
        )

        MaterialTheme(colors = themeColors, typography = typography) {
            children()
        }
    }
}

private fun DensityScope.textStyleFromTextAppearance(context: Context, @StyleRes id: Int): TextStyle {
    return context.obtainStyledAttributes(id, R.styleable.ComposeTextAppearance).use { a ->
        val color = a.getColor(R.styleable.ComposeTextAppearance_android_textColor, DEFAULT_COLOR)
        val textSize = a.getDimensionPixelSize(R.styleable.ComposeTextAppearance_android_textSize, -1)
        val textStyle = a.getInt(R.styleable.ComposeTextAppearance_android_textStyle, -1)
        val textFontWeight = a.getInt(R.styleable.ComposeTextAppearance_android_textFontWeight, -1)

        val typeface = a.getInt(R.styleable.ComposeTextAppearance_android_typeface, -1)

        val featureSettings = a.getString(R.styleable.ComposeTextAppearance_android_fontFeatureSettings)

        // TODO read and expand android:fontVariationSettings.
        // Not sure where it is used in Compose

        val shadowDx = a.getFloat(R.styleable.ComposeTextAppearance_android_shadowDx, 0f)
        val shadowDy = a.getFloat(R.styleable.ComposeTextAppearance_android_shadowDy, 0f)
        val shadowColor = a.getColor(R.styleable.ComposeTextAppearance_android_shadowColor, DEFAULT_COLOR)
        val shadowRadius = a.getFloat(R.styleable.ComposeTextAppearance_android_shadowRadius, 0f)

        val lineHeight = a.getDimensionPixelSize(R.styleable.ComposeTextAppearance_android_lineHeight, -1)

        fun readFontFamily(index: Int): FontFamily? {
            // TODO cache this TypedValue
            val tv = TypedValue()
            if (a.getValue(index, tv)) {
                return when (tv.type) {
                    TypedValue.TYPE_REFERENCE -> {
                        font(tv.data).asFontFamily()
                    }
                    else -> null
                }
            }
            return null
        }

        TextStyle(
            color = if (color != DEFAULT_COLOR) Color(color) else null,
            fontSize = textSize.toSp(),
            lineHeight = if (lineHeight != -1) lineHeight.toSp() else TextUnit.Inherit,
            fontFamily = when {
                // FYI, this only works with static font files in assets
                a.hasValue(R.styleable.ComposeTextAppearance_android_fontFamily) -> {
                    readFontFamily(R.styleable.ComposeTextAppearance_android_fontFamily)
                }
                a.hasValue(R.styleable.ComposeTextAppearance_fontFamily) -> {
                    readFontFamily(R.styleable.ComposeTextAppearance_fontFamily)
                }
                // Values below are from frameworks/base attrs.xml
                typeface == 1 -> FontFamily.SansSerif
                typeface == 2 -> FontFamily.Serif
                typeface == 3 -> FontFamily.Monospace
                else -> null
            },
            fontStyle = if ((textStyle and Typeface.ITALIC) != 0) FontStyle.Italic else FontStyle.Normal,
            fontWeight = when {
                textFontWeight in 0..199 -> FontWeight.W100
                textFontWeight in 200..299 -> FontWeight.W200
                textFontWeight in 300..399 -> FontWeight.W300
                textFontWeight in 400..499 -> FontWeight.W400
                textFontWeight in 500..599 -> FontWeight.W500
                textFontWeight in 600..699 -> FontWeight.W600
                textFontWeight in 700..799 -> FontWeight.W700
                textFontWeight in 800..899 -> FontWeight.W800
                textFontWeight in 900..999 -> FontWeight.W900
                // else, check the text style
                (textStyle and Typeface.BOLD) != 0 -> FontWeight.Bold
                else -> null
            },
            fontFeatureSettings = featureSettings,
            shadow = if (shadowColor != DEFAULT_COLOR) {
                Shadow(Color(shadowColor), Offset(shadowDx, shadowDy), shadowRadius.px)
            } else {
                null
            },
            letterSpacing = if (a.hasValue(R.styleable.ComposeTextAppearance_android_letterSpacing)) {
                a.getFloat(R.styleable.ComposeTextAppearance_android_letterSpacing, 0f).em
            } else {
                TextUnit.Inherit
            }
        )
    }
}

fun Typography.merge(
    h1: TextStyle = TextStyle(),
    h2: TextStyle = TextStyle(),
    h3: TextStyle = TextStyle(),
    h4: TextStyle = TextStyle(),
    h5: TextStyle = TextStyle(),
    h6: TextStyle = TextStyle(),
    subtitle1: TextStyle = TextStyle(),
    subtitle2: TextStyle = TextStyle(),
    body1: TextStyle = TextStyle(),
    body2: TextStyle = TextStyle(),
    button: TextStyle = TextStyle(),
    caption: TextStyle = TextStyle(),
    overline: TextStyle = TextStyle()
) = copy(
    h1 = h1.merge(h1),
    h2 = h2.merge(h2),
    h3 = h3.merge(h3),
    h4 = h4.merge(h4),
    h5 = h5.merge(h5),
    h6 = h6.merge(h6),
    subtitle1 = subtitle1.merge(subtitle1),
    subtitle2 = subtitle2.merge(subtitle2),
    body1 = body1.merge(body1),
    body2 = body2.merge(body2),
    button = button.merge(button),
    caption = caption.merge(caption),
    overline = overline.merge(overline)
)
