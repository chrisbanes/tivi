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

import android.support.v4.util.ArraySet
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.entities.Show
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.toRxMaybe
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.RetryAfterTimeoutWithDelay
import retrofit2.HttpException
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
    private val active = ArraySet<Int>()

    fun startUpdate(show: TiviShow): Boolean {
        return show.needsUpdateFromTmdb() && !active.contains(show.tmdbId)
    }

    fun updateShow(tmdbId: Int): Single<TiviShow> {
        val networkSource = tmdb.tvService().tv(tmdbId).toRxSingle()
                .subscribeOn(schedulers.network)
                .retryWhen(RetryAfterTimeoutWithDelay(3, 1000, this::shouldRetry))
                .observeOn(schedulers.disk)
                .map {
                    val show = showDao.getShowWithTmdbIdSync(tmdbId) ?: TiviShow(title = it.name)
                    showDao.insertOrUpdateShow(
                            show.copy(
                                    title = it.name,
                                    tmdbId = it.id,
                                    summary = it.overview,
                                    tmdbBackdropPath = it.backdrop_path,
                                    tmdbPosterPath = it.poster_path,
                                    homepage = it.homepage,
                                    originalTitle = it.original_name,
                                    lastTmdbUpdate = Date()
                            )
                    )
                }
                .doOnDispose { active.remove(tmdbId) }

        active += tmdbId

        return networkSource
    }

    private fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
        is HttpException -> throwable.code() == 429
        is IOException -> true
        is IllegalStateException -> true
        else -> false
    }
}

@Singleton
class TraktShowFetcher @Inject constructor(
        private val showDao: TiviShowDao,
        private val trakt: TraktV2,
        private val schedulers: AppRxSchedulers
) {
    fun getShow(traktId: Int, entity: Show? = null): Maybe<TiviShow> {
        val dbSource = showDao.getShowWithTraktId(traktId)
                .subscribeOn(schedulers.disk)

        val fromEntity = entity?.let {
            Maybe.just(mapShow(entity))
                    .observeOn(schedulers.disk)
                    .map { showDao.getShowWithTraktIdSync(traktId) ?: showDao.insertOrUpdateShow(it) }
        } ?: Maybe.empty<TiviShow>()

        val networkSource = trakt.shows().summary(traktId.toString(), Extended.NOSEASONS).toRxMaybe()
                .subscribeOn(schedulers.network)
                .retryWhen(RetryAfterTimeoutWithDelay(3, 1000, this::shouldRetry))
                .observeOn(schedulers.disk)
                .map { showDao.insertOrUpdateShow(mapShow(it)) }

        return Maybe.concat(dbSource, fromEntity, networkSource).firstElement()
    }

    private fun mapShow(show: Show): TiviShow {
        return TiviShow(
                title = show.title,
                traktId = show.ids.trakt,
                tmdbId = show.ids.tmdb,
                summary = show.overview)
    }

    private fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
        is HttpException -> throwable.code() == 429
        is IOException -> true
        else -> false
    }
}
