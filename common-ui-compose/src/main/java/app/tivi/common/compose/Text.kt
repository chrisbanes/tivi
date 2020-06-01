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
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.currentTextStyle
import androidx.ui.graphics.Color
import androidx.ui.graphics.useOrElse
import androidx.ui.material.Emphasis
import androidx.ui.material.EmphasisAmbient
import androidx.ui.text.InlineTextContent
import androidx.ui.text.TextLayoutResult
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontStyle
import androidx.ui.text.style.TextAlign
import androidx.ui.text.style.TextDecoration
import androidx.ui.text.style.TextOverflow
import androidx.ui.unit.TextUnit

@Composable
fun TextWithEmphasis(
    text: String,
    emphasis: Emphasis = EmphasisAmbient.current.high,
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
    val textColor = color.useOrElse { style.color.useOrElse { contentColor() } }.let {
        when {
            // If the color is opaque (or near opaque), we should emphasize it
            it.alpha >= 0.99f -> emphasis.applyEmphasis(it)
            else -> it
        }
    }

    Text(
        text,
        modifier,
        textColor,
        fontSize,
        fontStyle,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        overflow,
        softWrap,
        maxLines,
        inlineContent,
        onTextLayout,
        style
    )
}
