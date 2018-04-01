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

package me.banes.chris.tivi

import com.uwetrottmann.trakt5.entities.Show
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.emptySubscribe
import me.banes.chris.tivi.inject.ApplicationLevel
import me.banes.chris.tivi.tmdb.TmdbShowFetcher
import me.banes.chris.tivi.trakt.TraktShowFetcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowFetcher @Inject constructor(
    @ApplicationLevel private val disposables: CompositeDisposable,
    private val traktShowFetcher: TraktShowFetcher,
    private val tmdbShowFetcher: TmdbShowFetcher
) {
    fun loadShowAsync(traktId: Int, show: Show? = null) {
        disposables += loadShow(traktId, show).emptySubscribe()
    }

    fun loadShow(traktId: Int, show: Show? = null): Single<TiviShow> {
        return traktShowFetcher.loadShow(traktId, show)
                .doOnSuccess {
                    if (it.needsUpdateFromTmdb()) {
                        refreshFromTmdb(it.tmdbId!!)
                    }
                }
                .toSingle()
    }

    fun updateShow(traktId: Int): Single<TiviShow> {
        return traktShowFetcher.updateShow(traktId)
                .doOnSuccess {
                    if (it.needsUpdateFromTmdb()) {
                        refreshFromTmdb(it.tmdbId!!)
                    }
                }
    }

    private fun refreshFromTmdb(tmdbId: Int) {
        disposables += tmdbShowFetcher.updateShow(tmdbId).emptySubscribe()
    }
}