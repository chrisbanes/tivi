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

import androidx.compose.Composable
import androidx.compose.ambientOf
import androidx.ui.core.Modifier
import androidx.ui.foundation.ContentColorAmbient
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.currentTextStyle
import androidx.ui.graphics.Color
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.painter.Painter
import androidx.ui.graphics.useOrElse
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.material.Emphasis
import androidx.ui.text.AnnotatedString
import androidx.ui.text.InlineTextContent
import androidx.ui.text.TextLayoutResult
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontStyle
import androidx.ui.text.style.TextAlign
import androidx.ui.text.style.TextDecoration
import androidx.ui.text.style.TextOverflow
import androidx.ui.unit.TextUnit

interface ContentColorTransform {
    fun apply(color: Color): Color
}

val NONE = object : ContentColorTransform {
    override fun apply(color: Color): Color = color
}

val ContentColorTransformationAmbient = ambientOf { NONE }

@Composable
fun contentColor(
    transform: ContentColorTransform = ContentColorTransformationAmbient.current
): Color = transform.apply(ContentColorAmbient.current)

// @Composable
// fun MaterialTheme(
//    colors: ColorPalette = androidx.ui.material.MaterialTheme.colors,
//    typography: Typography = androidx.ui.material.MaterialTheme.typography,
//    shapes: Shapes = androidx.ui.material.MaterialTheme.shapes,
//    content: @Composable () -> Unit
// ) {
//    Providers(
//        ContentColorTransformationAmbient provides EmphasisAmbient.current.high.toTransform()
//    ) {
//        // blah
//    }
// }

// This wouldn't be needed
fun Emphasis.toTransform() = object : ContentColorTransform {
    override fun apply(color: Color): Color = applyEmphasis(color)
}

/**
 * Overloads to use [contentColor] above.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unset,
    fontSize: TextUnit = TextUnit.Inherit,
    fontStyle: FontStyle? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Inherit,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Inherit,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = currentTextStyle()
) {
    Text(
        text = text,
        modifier = modifier,
        color = color.useOrElse { style.color.useOrElse { contentColor() } },
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unset,
    fontSize: TextUnit = TextUnit.Inherit,
    fontStyle: FontStyle? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Inherit,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Inherit,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = currentTextStyle()
) {
    Text(
        text = text,
        modifier = modifier,
        color = color.useOrElse { style.color.useOrElse { contentColor() } },
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        inlineContent = inlineContent,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
fun Icon(
    asset: VectorAsset,
    modifier: Modifier = Modifier,
    tint: Color = contentColor()
) {
    Icon(asset, modifier, tint)
}

@Composable
fun Icon(
    asset: ImageAsset,
    modifier: Modifier = Modifier,
    tint: Color = contentColor()
) {
    Icon(asset, modifier, tint)
}

@Composable
fun Icon(
    painter: Painter,
    modifier: Modifier = Modifier,
    tint: Color = contentColor()
) {
    Icon(painter, modifier, tint)
}
