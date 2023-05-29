// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.shows

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.lastrequests.EntityLastRequestStore
import app.tivi.data.models.Request
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class ShowLastRequestStore(
    dao: LastRequestDao,
) : EntityLastRequestStore(Request.SHOW_DETAILS, dao)
