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
import androidx.annotation.StyleRes
import androidx.compose.Composable
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.ui.core.DensityAmbient
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.foundation.shape.corner.CornerBasedShape
import androidx.ui.foundation.shape.corner.CornerSize
import androidx.ui.foundation.shape.corner.CutCornerShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Shadow
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Shapes
import androidx.ui.material.Typography
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import androidx.ui.unit.em
import androidx.ui.unit.px

/**
 * A [MaterialTheme] which reads the corresponding values from an
 * Material Design Components Android theme in the given [context].
 *
 * By default the text colors from any associated `TextAppearance`s from the theme are *not* read.
 * This is because setting a fixed color in the resulting [TextStyle] breaks the usage of
 * [androidx.ui.material.Emphasis] through [androidx.ui.material.ProvideEmphasis].
 * You can customize this through the [useTextColors] parameter.
 *
 * @param context The context to read the theme from
 * @param useTextColors whether to read the colors from the `TextAppearance`s associated from the
 * theme. Defaults to `false`
 */
@Composable
fun MaterialThemeFromAndroidTheme(
    context: Context,
    readColors: Boolean = true,
    readTypography: Boolean = true,
    readShapes: Boolean = true,
    useTextColors: Boolean = false,
    children: @Composable() () -> Unit
) {
    val (colors, type, shapes) = generateMaterialThemeFromContext(
        context,
        readColors,
        readTypography,
        readShapes,
        useTextColors
    )

    MaterialTheme(
        typography = type,
        colors = colors,
        shapes = shapes,
        content = children
    )
}

/**
 * This effect generates the components of an [androidx.ui.material.MaterialTheme], reading the
 * values from an Material Design Components Android theme.
 *
 * By default the text colors from any associated `TextAppearance`s from the theme are *not* read.
 * This is because setting a fixed color in the resulting [TextStyle] breaks the usage of
 * [androidx.ui.material.Emphasis] through [androidx.ui.material.ProvideEmphasis].
 * You can customize this through the [useTextColors] parameter.
 *
 * @param context The context to read the theme from
 * @param useTextColors whether to read the colors from the `TextAppearance`s associated from the
 * theme. Defaults to `false`
 */
@Composable
fun generateMaterialThemeFromContext(
    context: Context,
    readColors: Boolean = true,
    readTypography: Boolean = true,
    readShapes: Boolean = true,
    useTextColors: Boolean = false
): Triple<ColorPalette, Typography, Shapes> {
    return context.obtainStyledAttributes(R.styleable.ComposeTheme).use { ta ->

        val colors: ColorPalette = if (readColors) {
            /* First we'll read the Material color palette */
            val primary = ta.getComposeColor(R.styleable.ComposeTheme_colorPrimary)
            val primaryVariant = ta.getComposeColor(R.styleable.ComposeTheme_colorPrimaryVariant)
            val onPrimary = ta.getComposeColor(R.styleable.ComposeTheme_colorOnPrimary)
            val secondary = ta.getComposeColor(R.styleable.ComposeTheme_colorSecondary)
            val onSecondary = ta.getComposeColor(R.styleable.ComposeTheme_colorOnSecondary)
            val background = ta.getComposeColor(R.styleable.ComposeTheme_android_colorBackground)
            val onBackground = ta.getComposeColor(R.styleable.ComposeTheme_colorOnBackground)
            val surface = ta.getComposeColor(R.styleable.ComposeTheme_colorSurface)
            val onSurface = ta.getComposeColor(R.styleable.ComposeTheme_colorOnSurface)

            val isLightTheme = ta.getBoolean(R.styleable.ComposeTheme_isLightTheme, true)

            if (isLightTheme) {
                lightColorPalette(
                    primary = primary,
                    primaryVariant = primaryVariant,
                    onPrimary = onPrimary,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface
                )
            } else {
                darkColorPalette(
                    primary = primary,
                    primaryVariant = primaryVariant,
                    onPrimary = onPrimary,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface
                )
            }
        } else {
            // Else we create an empty color palette based on the configuration's uiMode
            if (isSystemInDarkTheme()) darkColorPalette() else lightColorPalette()
        }

        /**
         * Next we'll generate a typography instance, using the Material Theme text appearances
         * for TextStyles.
         *
         * We create a normal 'empty' instance first to start from the defaults, then merge in our
         * generated text styles from the Android theme.
         */
        var typography = Typography()

        if (readTypography) {
            typography = typography.merge(
                h1 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline1),
                    useTextColors
                ),
                h2 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline2),
                    useTextColors
                ),
                h3 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline3),
                    useTextColors
                ),
                h4 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline4),
                    useTextColors
                ),
                h5 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline5),
                    useTextColors
                ),
                h6 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceHeadline6),
                    useTextColors
                ),
                subtitle1 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceSubtitle1),
                    useTextColors
                ),
                subtitle2 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceSubtitle2),
                    useTextColors
                ),
                body1 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceBody1),
                    useTextColors
                ),
                body2 = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceBody2),
                    useTextColors
                ),
                button = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceButton),
                    useTextColors
                ),
                caption = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceCaption),
                    useTextColors
                ),
                overline = textStyleFromTextAppearance(
                    context,
                    ta.getResourceIdOrThrow(R.styleable.ComposeTheme_textAppearanceOverline),
                    useTextColors
                )
            )
        }

        /**
         * Now read the shape appearances
         */
        val shapes = if (readShapes) {
            Shapes(
                small = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeTheme_shapeAppearanceSmallComponent),
                    fallbackSize = CornerSize(4.dp)
                ),
                medium = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeTheme_shapeAppearanceMediumComponent),
                    fallbackSize = CornerSize(4.dp)
                ),
                large = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeTheme_shapeAppearanceLargeComponent),
                    fallbackSize = CornerSize(0.dp)
                )
            )
        } else {
            Shapes()
        }

        Triple(colors, typography, shapes)
    }
}

@Composable
private fun textStyleFromTextAppearance(
    context: Context,
    @StyleRes id: Int,
    useTextColor: Boolean
): TextStyle {
    return context.obtainStyledAttributes(id, R.styleable.ComposeTextAppearance).use { a ->
        val textStyle = a.getInt(R.styleable.ComposeTextAppearance_android_textStyle, -1)
        val textFontWeight = a.getInt(R.styleable.ComposeTextAppearance_android_textFontWeight, -1)
        val typeface = a.getInt(R.styleable.ComposeTextAppearance_android_typeface, -1)

        // TODO read and expand android:fontVariationSettings.
        // Variable fonts are not supported in Compose yet

        val density = DensityAmbient.current

        TextStyle(
            color = if (useTextColor) {
                a.getComposeColorOrNull(R.styleable.ComposeTextAppearance_android_textColor)
            } else null,
            fontSize = a.getTextUnit(density, R.styleable.ComposeTextAppearance_android_textSize),
            lineHeight = a.getTextUnit(density, R.styleable.ComposeTextAppearance_android_lineHeight),
            fontFamily = when {
                // FYI, this only works with static font files in assets
                a.hasValue(R.styleable.ComposeTextAppearance_android_fontFamily) -> {
                    a.getFontFamilyOrNull(R.styleable.ComposeTextAppearance_android_fontFamily)
                }
                a.hasValue(R.styleable.ComposeTextAppearance_fontFamily) -> {
                    a.getFontFamilyOrNull(R.styleable.ComposeTextAppearance_fontFamily)
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
            fontFeatureSettings = a.getString(R.styleable.ComposeTextAppearance_android_fontFeatureSettings),
            shadow = run {
                val shadowColor = a.getComposeColorOrNull(R.styleable.ComposeTextAppearance_android_shadowColor)
                if (shadowColor != null) {
                    val shadowDx = a.getFloat(R.styleable.ComposeTextAppearance_android_shadowDx, 0f)
                    val shadowDy = a.getFloat(R.styleable.ComposeTextAppearance_android_shadowDy, 0f)
                    val shadowRadius = a.getFloat(R.styleable.ComposeTextAppearance_android_shadowRadius, 0f)
                    Shadow(
                        color = shadowColor,
                        offset = Offset(shadowDx, shadowDy),
                        blurRadius = shadowRadius.px
                    )
                } else null
            },
            letterSpacing = when {
                a.hasValue(R.styleable.ComposeTextAppearance_android_letterSpacing) -> {
                    a.getFloat(R.styleable.ComposeTextAppearance_android_letterSpacing, 0f).em
                }
                else -> TextUnit.Inherit
            }
        )
    }
}

@Composable
private fun readShapeAppearance(
    context: Context,
    @StyleRes id: Int,
    fallbackSize: CornerSize
): CornerBasedShape {
    return context.obtainStyledAttributes(id, R.styleable.ComposeShapeAppearance).use { a ->
        val defaultCornerSize = a.getCornerSize(
            R.styleable.ComposeShapeAppearance_cornerSize,
            fallbackSize
        )
        val cornerSizeTL = a.getCornerSizeOrNull(
            R.styleable.ComposeShapeAppearance_cornerSizeTopLeft)
        val cornerSizeTR = a.getCornerSizeOrNull(
            R.styleable.ComposeShapeAppearance_cornerSizeTopRight)
        val cornerSizeBL = a.getCornerSizeOrNull(
            R.styleable.ComposeShapeAppearance_cornerSizeBottomLeft)
        val cornerSizeBR = a.getCornerSizeOrNull(
            R.styleable.ComposeShapeAppearance_cornerSizeBottomRight)

        /**
         * We do not support the individual `cornerFamilyTopLeft`, etc, since Compose only supports
         * one corner type per shape. Therefore we only read the `cornerFamily` attribute.
         */
        when (a.getInt(R.styleable.ComposeShapeAppearance_cornerFamily, 0)) {
            0 -> {
                RoundedCornerShape(
                    topLeft = cornerSizeTL ?: defaultCornerSize,
                    topRight = cornerSizeTR ?: defaultCornerSize,
                    bottomRight = cornerSizeBR ?: defaultCornerSize,
                    bottomLeft = cornerSizeBL ?: defaultCornerSize
                )
            }
            1 -> {
                CutCornerShape(
                    topLeft = cornerSizeTL ?: defaultCornerSize,
                    topRight = cornerSizeTR ?: defaultCornerSize,
                    bottomRight = cornerSizeBR ?: defaultCornerSize,
                    bottomLeft = cornerSizeBL ?: defaultCornerSize
                )
            }
            else -> throw IllegalArgumentException("Unknown cornerFamily set in ShapeAppearance")
        }
    }
}

private fun Typography.merge(
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
