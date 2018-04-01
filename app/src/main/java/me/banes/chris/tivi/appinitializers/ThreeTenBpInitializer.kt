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

package me.banes.chris.tivi.appinitializers

import android.app.Application
import com.gabrielittner.threetenbp.LazyThreeTen
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import me.banes.chris.tivi.inject.ApplicationLevel
import me.banes.chris.tivi.util.AppRxSchedulers
import timber.log.Timber
import javax.inject.Inject

class ThreeTenBpInitializer @Inject constructor(
    @ApplicationLevel private val disposables: CompositeDisposable,
    private val schedulers: AppRxSchedulers
) : AppInitializer {
    override fun init(application: Application) {
        // Init LazyThreeTen
        LazyThreeTen.init(application)

        // ...and cache it's timezones on a background thread
        disposables += Completable.fromCallable { LazyThreeTen.cacheZones() }
                .subscribeOn(schedulers.disk)
                .subscribe({
                    // Ignore, nothing to do here!
                }, {
                    // This should never happen so lets throw the exception
                    Timber.e(it)
                    throw it
                })
    }
}