// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.store

import app.tivi.data.licenses.LicenseItem
import app.tivi.data.licenses.fetcher.LicensesFetcher
import co.touchlab.kermit.Logger
import me.tatarka.inject.annotations.Inject

@Inject
class LicensesStoreImpl(
  private val fetcher: Lazy<LicensesFetcher>,
) : LicensesStore {
  private var licenses: List<LicenseItem>? = null

  override suspend fun getLicenses(): List<LicenseItem> {
    return licenses ?: try {
      fetcher.value.invoke()
        .filterNot(::isKmpTargetDependency)
        .also { licenses = it }
    } catch (e: Exception) {
      Logger.e(e) { "Exception whilst fetching licenses" }
      emptyList()
    }
  }

  private companion object {
    val TARGET_SUFFIXES = setOf(
      "iosarm64",
      "iossimulatorarm64",
      "android",
      "jvm",
    )

    fun isKmpTargetDependency(item: LicenseItem): Boolean {
      return TARGET_SUFFIXES.any { suffix ->
        item.artifactId.endsWith(suffix)
      }
    }
  }
}
