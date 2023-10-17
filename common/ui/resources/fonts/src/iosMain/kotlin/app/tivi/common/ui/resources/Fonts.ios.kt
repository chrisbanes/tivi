// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.posix.memcpy

private val cache: MutableMap<String, Font> = mutableMapOf()

@Composable
actual fun font(fontName: String, resourceId: String, weight: FontWeight, style: FontStyle): Font {
  return cache.getOrPut(resourceId) {
    // Ideally we'd use the resource() API here, but it doesn't seem to be working
    // in multi-module projects
    androidx.compose.ui.text.platform.Font(
      identity = resourceId,
      data = readBundleFile("$resourceId.ttf"),
      weight = weight,
      style = style,
    )
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun readBundleFile(path: String): ByteArray {
  val fileManager = NSFileManager.defaultManager()
  val composeResourcesPath = NSBundle.mainBundle.resourcePath + "/" + path
  val contentsAtPath: NSData? = fileManager.contentsAtPath(composeResourcesPath)
  if (contentsAtPath != null) {
    val byteArray = ByteArray(contentsAtPath.length.toInt())
    byteArray.usePinned {
      memcpy(it.addressOf(0), contentsAtPath.bytes, contentsAtPath.length)
    }
    return byteArray
  } else {
    error("File $path not found in Bundle")
  }
}
