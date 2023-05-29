// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.watchedshows

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.lastrequests.GroupLastRequestStore
import app.tivi.data.models.Request
import me.tatarka.inject.annotations.Inject

@Inject
class WatchedShowsLastRequestStore(
    dao: LastRequestDao,
) : GroupLastRequestStore(Request.WATCHED_SHOWS, dao)
