/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.ColorResource

@Composable
fun colorResource(resource: ColorResource): Color {
    val context: Context = LocalContext.current
    return Color(resource.getColor(context))
}
