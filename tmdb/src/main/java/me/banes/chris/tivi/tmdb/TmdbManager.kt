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

package me.banes.chris.tivi.tmdb

import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.tmdb2.entities.Configuration
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbManager @Inject constructor(
    schedulers: AppRxSchedulers,
    tmdbClient: Tmdb
) {

    private val disposables = CompositeDisposable()
    private val imageProviderSubject = BehaviorSubject.createDefault(TmdbImageUrlProvider())!!

    val imageProvider = imageProviderSubject.toFlowable(BackpressureStrategy.LATEST)!!

    init {
        disposables += tmdbClient.configurationService().configuration().toRxSingle()
                .subscribeOn(schedulers.network)
                .observeOn(schedulers.main)
                .subscribe(this::onConfigurationLoaded, this::onError)
    }

    private fun onConfigurationLoaded(configuration: Configuration) {
        configuration.images?.let {
            val newProvider = TmdbImageUrlProvider(
                    it.secure_base_url,
                    it.poster_sizes.toTypedArray(),
                    it.backdrop_sizes.toTypedArray())
            imageProviderSubject.onNext(newProvider)
        }
    }

    private fun onError(t: Throwable) = Timber.e(t, "Error while fetching configuration from TMDb")
}