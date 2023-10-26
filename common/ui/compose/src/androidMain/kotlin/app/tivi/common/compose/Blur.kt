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
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

actual fun Modifier.glassBlur(
  areas: List<Rect>,
  color: Color,
  blurRadius: Float,
): Modifier {
  if (Build.VERSION.SDK_INT < 31) {
    // On older platforms we just display a translucent scrim
    return drawWithContent {
      drawContent()

      for (area in areas) {
        drawRect(color = color, topLeft = area.topLeft, size = area.size, alpha = 0.9f)
      }
    }
  }

  return drawWithCache {
    // This is our RenderEffect. It first applies a blur effect, and then a color filter effect
    // to allow content to be visible on top
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

    // We create a RenderNode for each of the areas we need to apply our effect to
    val effectRenderNodes = areas.map { area ->
      // We expand the area where our effect is applied to. This is necessary so that the blur
      // effect is applied evenly to allow edges. If we don't do this, the blur effect is much less
      // visible on the edges of the area.
      val expandedRect = area.inflate(blurRadius)

      val node = RenderNode("blur").apply {
        setRenderEffect(effect)
        setPosition(0, 0, expandedRect.width.toInt(), expandedRect.height.toInt())
        translationX = expandedRect.left
        translationY = expandedRect.top
      }
      EffectRenderNodeHolder(renderNode = node, renderNodeDrawArea = expandedRect, area = area)
    }

    onDrawWithContent {
      // First we draw the composable content into `contentNode`
      Canvas(contentNode.beginRecording()).also { canvas ->
        draw(this, layoutDirection, canvas, size) {
          this@onDrawWithContent.drawContent()
        }
        contentNode.endRecording()
      }

      // Now we draw `contentNode` into the window canvas, so that it is displayed
      drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRenderNode(contentNode)
      }

      // Now we need to draw `contentNode` into each of our 'effect' RenderNodes, allowing
      // their RenderEffect to be applied to the composable content.
      effectRenderNodes.forEach { effect ->
        effect.renderNode.beginRecording().also { canvas ->
          // We need to draw our background color first, as the `contentNode` may not draw
          // a background. This then makes the blur effect much less pronounced, as blurring with
          // transparent negates the effect.
          canvas.drawColor(color.toArgb())
          canvas.translate(-effect.renderNodeDrawArea.left, -effect.renderNodeDrawArea.top)
          canvas.drawRenderNode(contentNode)
          effect.renderNode.endRecording()
        }
      }

      // Finally we draw each 'effect' RenderNode to the window canvas, drawing on top
      // of the original content
      drawIntoCanvas { canvas ->
        effectRenderNodes.forEach { effect ->
          val (node, _, area) = effect
          clipRect(area.left, area.top, area.right, area.bottom) {
            canvas.nativeCanvas.drawRenderNode(node)
          }
        }
      }
    }
  }
}

private data class EffectRenderNodeHolder(
  val renderNode: RenderNode,
  val renderNodeDrawArea: Rect,
  val area: Rect,
)
