// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.fetcher

import android.app.Application
import app.tivi.data.licenses.LicenseItem
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidLicensesFetcherImpl(private val context: Application): LicensesFetcher{
    @kotlinx.serialization.ExperimentalSerializationApi
    override suspend fun fetch(): List<LicenseItem>{
        var licenseItemList: List<LicenseItem> = emptyList()
        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = context.assets.open("artifacts.json")
                val json = Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
                licenseItemList = json.decodeFromStream(inputStream)
            } catch (ex: IOException) {
                licenseItemList = emptyList()
            }
        }
        return licenseItemList
    }
}
