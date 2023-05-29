// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.models.LastRequest
import app.tivi.data.models.Request

interface LastRequestDao : EntityDao<LastRequest> {

    fun lastRequest(request: Request, entityId: Long): LastRequest?

    fun requestCount(request: Request, entityId: Long): Int
}
