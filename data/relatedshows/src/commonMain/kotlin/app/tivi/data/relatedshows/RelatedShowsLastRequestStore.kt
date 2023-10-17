// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.relatedshows

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.lastrequests.EntityLastRequestStore
import app.tivi.data.models.Request
import me.tatarka.inject.annotations.Inject

@Inject
class RelatedShowsLastRequestStore(
  dao: LastRequestDao,
) : EntityLastRequestStore(Request.RELATED_SHOWS, dao)
