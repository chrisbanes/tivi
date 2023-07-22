package com.vanpra.composematerialdialogs.datetime.util

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal actual fun isSmallDevice(): Boolean {
    return LocalConfiguration.current.screenWidthDp <= 360
}

@Composable
internal actual fun isLargeDevice(): Boolean {
    return LocalConfiguration.current.screenWidthDp <= 600
}

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
    val outerText = Paint()
    outerText.color = color.toArgb()
    outerText.textSize = textSize
    outerText.textAlign =
        if (isCenter == true) Paint.Align.CENTER else if (isCenter == false) Paint.Align.LEFT else Paint.Align.RIGHT
    outerText.alpha = maxOf(0, minOf(alpha * 255, 255))

    val r = Rect()
    outerText.getTextBounds(text, 0, text.length, r)

    nativeCanvas.drawText(
        text,
        x + (radius * cos(angle)),
        y + (radius * sin(angle)) + (abs(r.height())) / 2,
        outerText
    )
}