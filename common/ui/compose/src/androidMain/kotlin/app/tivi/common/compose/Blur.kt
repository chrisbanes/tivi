// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

actual fun Modifier.glassBlur(
  area: Rect,
  color: Color,
  blurRadius: Float,
): Modifier {
  // We disable this everywhere right now as we can't use the draw() function on DrawScope.
  // Waiting for Compose 1.6.0 APIs to land in CMP

  if (Build.VERSION.SDK_INT < 99) {
    // On older platforms we just display a translucent scrim
    return drawWithContent {
      drawContent()
      drawRect(color = color, topLeft = area.topLeft, size = area.size, alpha = 0.85f)
    }
  }

  return drawWithCache {
    val effect = RenderEffect.createColorFilterEffect(
      BlendModeColorFilter(
        color.copy(alpha = 0.7f).toArgb(),
        BlendMode.SRC_OVER,
      ),
      RenderEffect.createBlurEffect(
        blurRadius,
        blurRadius,
        Shader.TileMode.DECAL,
      ),
    )

    val contentNode = RenderNode("content").apply {
      setPosition(0, 0, size.width.toInt(), size.height.toInt())
    }

    val expandedRect = area.inflate(blurRadius).intersect(size.toRect())

    val blurNode = RenderNode("blur").apply {
      setRenderEffect(effect)
      setPosition(0, 0, expandedRect.width.toInt(), expandedRect.height.toInt())
      translationX = expandedRect.left
      translationY = expandedRect.top
    }

    onDrawWithContent {
      Canvas(contentNode.beginRecording()).also { // canvas ->
        // This isn't available until Compose 1.6.0
        // draw(this, layoutDirection, canvas, size) {
        this@onDrawWithContent.drawContent()
        // }
        contentNode.endRecording()
      }

      drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRenderNode(contentNode)
      }

      blurNode.beginRecording().also { canvas ->
        canvas.translate(-expandedRect.left, -expandedRect.top)
        canvas.drawRenderNode(contentNode)
        blurNode.endRecording()
      }

      drawIntoCanvas { canvas ->
        clipRect(area.left, area.top, area.right, area.bottom) {
          canvas.nativeCanvas.drawRenderNode(blurNode)
        }
      }
    }
  }
}
