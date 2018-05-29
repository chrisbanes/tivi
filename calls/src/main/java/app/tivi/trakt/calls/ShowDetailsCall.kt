/*
 * Copyright 2017 Google, Inc.
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

package app.tivi.trakt.calls

import io.reactivex.Flowable
import app.tivi.ShowFetcher
import app.tivi.calls.Call
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.TiviShow
import app.tivi.util.AppRxSchedulers
import javax.inject.Inject

class ShowDetailsCall @Inject constructor(
    private val dao: TiviShowDao,
    private val showFetcher: ShowFetcher,
    private val schedulers: AppRxSchedulers
) : Call<Long, TiviShow> {
    override suspend fun refresh(param: Long) {
        showFetcher.update(param)
    }

    override fun data(param: Long): Flowable<TiviShow> {
        return dao.getShowWithIdFlowable(param)
                .subscribeOn(schedulers.database)
                .distinctUntilChanged()
    }
}
