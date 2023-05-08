/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import android.content.Context
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import dev.icerock.moko.resources.FontResource

@Composable
fun fontFamilyResource(fontResource: FontResource): FontFamily {
    val context: Context = LocalContext.current
    return remember(context, fontResource) {
        val typeface: Typeface = fontResource.getTypeface(context)
            ?: throw IllegalStateException("can't read typeface for $fontResource")

        FontFamily(typeface)
    }
}
