package com.vanpra.composematerialdialogs.datetime.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextBlobBuilder

@Composable
internal actual fun isSmallDevice(): Boolean {
    return true
}

@Composable
internal actual fun isLargeDevice(): Boolean {
    return false
}

// todo This function needs to be corrected
internal actual fun Canvas.drawText(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    textSize: Float,
    angle: Float,
    radius: Float,
    isCenter: Boolean?,
    alpha: Int,
) {
    val font = Font().apply {
        size = textSize
    }

    nativeCanvas.drawTextBlob(
        blob = TextBlobBuilder().apply {
            appendRun(font = font, text = text, x = 0f, y = 0f)
        }.build()!!,
        x = x + (radius * cos(angle)),
        y = y + (radius * sin(angle)) + (abs(font.metrics.height)) / 2,
        paint = Paint().apply {
            this.color = color.toArgb()
        }
    )
}
