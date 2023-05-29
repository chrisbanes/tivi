// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
