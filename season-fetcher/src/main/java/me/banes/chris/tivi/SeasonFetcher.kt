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

import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.banes.chris.tivi.data.entities.Season
import me.banes.chris.tivi.extensions.emptySubscribe
import me.banes.chris.tivi.inject.ApplicationLevel
import me.banes.chris.tivi.trakt.TraktSeasonFetcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeasonFetcher @Inject constructor(
    @ApplicationLevel private val disposables: CompositeDisposable,
    private val traktSeasonFetcher: TraktSeasonFetcher,
    private val episodeFetcher: EpisodeFetcher
) {
    fun loadAsync(showId: Long) {
        disposables += load(showId).emptySubscribe()
    }

    fun load(showId: Long): Maybe<List<Season>> {
        return traktSeasonFetcher.loadShowSeasons(showId)
                .doOnSuccess {
                    it.forEach {
                        episodeFetcher.loadAsync(it.id!!)
                    }
                }
    }
}