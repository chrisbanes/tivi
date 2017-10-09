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

package me.banes.chris.tivi.calls

import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.tmdb2.entities.TvShow
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.data.TiviShowDao
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.RetryAfterTimeoutWithDelay
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbShowFetcher @Inject constructor(
        private val showDao: TiviShowDao,
        private val tmdb: Tmdb,
        private val schedulers: AppRxSchedulers
) {
    fun showFromTmdb(tmdbId: Int, traktId: Int): Maybe<TiviShow> {
        val dbSource = showDao.getShowFromId(tmdbId, traktId)
                .subscribeOn(schedulers.disk)
                .filter { !it.needsUpdateFromTmdb() } // Don't emit if the item needs updating

        val networkSource = tmdb.tvService().tv(tmdbId).toRxSingle()
                .subscribeOn(schedulers.network)
                .observeOn(schedulers.disk)
                .retryWhen(RetryAfterTimeoutWithDelay(3, 1000, this::shouldRetry))
                .flatMap { mapTmdbShow(it) }
                .map {
                    var show = it
                    if (show.traktId == null) {
                        show = show.copy(traktId = traktId)
                    }
                    if (it.id == null) {
                        Timber.d("Inserting show: %s", show)
                        show = show.copy(id = showDao.insertShow(show))
                    } else {
                        Timber.d("Updating show: %s", show)
                        showDao.updateShow(show)
                    }
                    show
                }

        return Maybe.concat(dbSource, networkSource.toMaybe()).firstElement()
    }

    private fun mapTmdbShow(tmdbShow: TvShow): Single<TiviShow> {
        return showDao.getShowWithTmdbId(tmdbShow.id)
                .subscribeOn(schedulers.disk)
                .toSingle(TiviShow(title = tmdbShow.name))
                .map {
                    it.copy(
                            title = tmdbShow.name,
                            tmdbId = tmdbShow.id,
                            summary = tmdbShow.overview,
                            tmdbBackdropPath = tmdbShow.backdrop_path,
                            tmdbPosterPath = tmdbShow.poster_path,
                            homepage = tmdbShow.homepage,
                            originalTitle = tmdbShow.original_name,
                            lastTmdbUpdate = Date()
                    )
                }
    }

    private fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
        is HttpException -> throwable.code() == 429
        is IOException -> true
        else -> false
    }
}
