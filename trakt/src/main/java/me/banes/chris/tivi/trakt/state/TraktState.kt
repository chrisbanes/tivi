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

package me.banes.chris.tivi.trakt.state

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktState @Inject constructor() {
    private val relatedShows = mutableMapOf<Int, List<Int>>()
    private val relatedShowsObservable = BehaviorSubject.createDefault(relatedShows)

    fun relatedShowsForTraktId(id: Int): Flowable<List<Int>> {
        return relatedShowsObservable
                .map { it[id] ?: emptyList() }
                .distinctUntilChanged()
                .toFlowable(BackpressureStrategy.LATEST)
    }

    fun setRelatedShowsForTraktId(id: Int, shows: List<Int>) {
        relatedShows[id] = shows
        relatedShowsObservable.onNext(relatedShows)
    }
}