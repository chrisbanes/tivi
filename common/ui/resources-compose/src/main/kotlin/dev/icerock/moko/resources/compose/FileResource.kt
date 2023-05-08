/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.icerock.moko.resources.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.FileResource

@Composable
fun FileResource.readTextAsState(): State<String?> {
    val context: Context = LocalContext.current
    return produceState<String?>(null, this, context) {
        value = readText(context)
    }
}
