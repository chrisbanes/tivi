// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.anticipatedshows

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.lastrequests.GroupLastRequestStore
import app.tivi.data.models.Request
import me.tatarka.inject.annotations.Inject

@Inject
class AnticipatedShowsLastRequestStore(
  dao: LastRequestDao,
) : GroupLastRequestStore(Request.ANTICIPATED_SHOWS, dao)
