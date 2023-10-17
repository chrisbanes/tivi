// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.fetcher

import app.tivi.data.licenses.LicenseItem
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.posix.memcpy

@OptIn(ExperimentalSerializationApi::class)
@Inject
class IosLicensesFetcherImpl(
  private val dispatchers: AppCoroutineDispatchers,
) : LicensesFetcher {
  override suspend fun invoke(): List<LicenseItem> = withContext(dispatchers.io) {
    val json = Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }
    json.decodeFromString(readBundleFile("licenses.json").decodeToString())
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
