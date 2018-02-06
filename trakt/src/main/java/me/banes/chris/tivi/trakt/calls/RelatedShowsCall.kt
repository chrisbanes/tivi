/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.banes.chris.tivi.trakt.calls

import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Completable
import io.reactivex.Flowable
import me.banes.chris.tivi.calls.Call
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.trakt.TraktShowFetcher
import me.banes.chris.tivi.trakt.state.TraktState
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

class RelatedShowsCall @Inject constructor(
    private val dao: TiviShowDao,
    private val traktState: TraktState,
    private val traktShowFetcher: TraktShowFetcher,
    private val trakt: TraktV2,
    private val schedulers: AppRxSchedulers
) : Call<Long, List<TiviShow>> {
    override fun refresh(param: Long): Completable {
        return dao.getShowWithIdMaybe(param)
                .subscribeOn(schedulers.database)
                .map(TiviShow::traktId)
                .flatMapSingle { traktId ->
                    trakt.shows().related(traktId.toString(), 0, 10, Extended.NOSEASONS).toRxSingle()
                            .subscribeOn(schedulers.network)
                            .map {
                                traktState.setRelatedShowsForTraktId(traktId, it.map { it.ids.trakt })
                            }
                }
                .toCompletable()
    }

    override fun data(param: Long): Flowable<List<TiviShow>> {
        return return dao.getShowWithIdMaybe(param)
                .subscribeOn(schedulers.database)
                .map(TiviShow::traktId)
                .flatMapPublisher(traktState::relatedShowsForTraktId)
                .flatMapIterable { it }
                .flatMapMaybe { traktShowFetcher.getShow(it) }
                .toList()
                .toFlowable()
    }
}