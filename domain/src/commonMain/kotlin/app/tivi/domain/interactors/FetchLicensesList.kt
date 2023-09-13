// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.licenses.LicenseItem
import app.tivi.data.licenses.store.LicensesStore
import app.tivi.domain.Interactor
import me.tatarka.inject.annotations.Inject

@Inject
class FetchLicensesList(
    private val licensesStore: LicensesStore,
) : Interactor<Unit, List<LicenseItem>>() {

    override suspend fun doWork(params: Unit): List<LicenseItem> {
        return licensesStore.getOpenSourceItemList()
    }
}
