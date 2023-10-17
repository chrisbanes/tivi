// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.store

import app.tivi.data.licenses.LicenseItem
import app.tivi.data.licenses.fetcher.LicensesFetcher
import app.tivi.util.Logger
import me.tatarka.inject.annotations.Inject

@Inject
class LicensesStoreImpl(
  private val fetcher: LicensesFetcher,
  private val logger: Logger,
) : LicensesStore {
  private var licenses: List<LicenseItem>? = null

  override suspend fun getLicenses(): List<LicenseItem> {
    return licenses ?: try {
      fetcher().also { licenses = it }
    } catch (e: Exception) {
      logger.e(e) { "Exception whilst fetching licenses" }
      emptyList()
    }
  }
}
