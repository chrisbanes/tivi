// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.fetcher

import android.app.Application
import app.tivi.data.licenses.LicenseItem
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidLicensesFetcherImpl(
  private val context: Application,
  private val dispatchers: AppCoroutineDispatchers,
) : LicensesFetcher {
  @ExperimentalSerializationApi
  override suspend fun invoke(): List<LicenseItem> = withContext(dispatchers.io) {
    val json = Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }
    json.decodeFromStream(context.assets.open("licenses.json"))
  }
}
