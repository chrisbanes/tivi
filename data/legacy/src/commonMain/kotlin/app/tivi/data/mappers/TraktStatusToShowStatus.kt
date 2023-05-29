// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktShowStatus
import app.tivi.data.models.ShowStatus
import me.tatarka.inject.annotations.Inject

@Inject
class TraktStatusToShowStatus : Mapper<TraktShowStatus, ShowStatus> {

    override fun map(from: TraktShowStatus): ShowStatus = when (from) {
        TraktShowStatus.ENDED -> ShowStatus.ENDED
        TraktShowStatus.RETURNING_SERIES -> ShowStatus.RETURNING
        TraktShowStatus.CANCELED -> ShowStatus.CANCELED
        TraktShowStatus.IN_PRODUCTION -> ShowStatus.IN_PRODUCTION
        TraktShowStatus.PLANNED -> ShowStatus.PLANNED
    }
}
