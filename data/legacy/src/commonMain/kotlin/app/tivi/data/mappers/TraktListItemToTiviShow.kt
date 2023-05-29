// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktUserListItem
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktListItemToTiviShow(
    private val showMapper: TraktShowToTiviShow,
) : Mapper<TraktUserListItem, TiviShow> {
    override fun map(from: TraktUserListItem) = showMapper.map(from.show!!)
}
