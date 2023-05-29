// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.trendingshows

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.lastrequests.GroupLastRequestStore
import app.tivi.data.models.Request
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class TrendingShowsLastRequestStore(
    dao: LastRequestDao,
) : GroupLastRequestStore(Request.TRENDING_SHOWS, dao)
