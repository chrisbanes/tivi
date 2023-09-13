// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.fetcher

import android.app.Application
import app.tivi.data.licenses.LicenseItem
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidLicensesFetcherImpl(private val context: Application) : LicensesFetcher {
    @ExperimentalSerializationApi
    override suspend fun invoke(): List<LicenseItem> = withContext(Dispatchers.IO) {
        try {
            val json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
            json.decodeFromStream(context.assets.open("artifacts.json"))
        } catch (ex: IOException) {
            emptyList()
        }
    }
}
