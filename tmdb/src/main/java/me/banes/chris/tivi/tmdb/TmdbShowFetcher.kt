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

import android.support.v4.util.ArraySet
import com.uwetrottmann.tmdb2.Tmdb
import io.reactivex.Completable
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.RetryAfterTimeoutWithDelay
import org.threeten.bp.OffsetDateTime
import retrofit2.HttpException
import java.io.IOException
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

    fun updateShow(tmdbId: Int): Completable {
        if (active.contains(tmdbId)) {
            return Completable.complete()
        }
        return tmdb.tvService().tv(tmdbId).toRxSingle()
                .subscribeOn(schedulers.network)
                .retryWhen(RetryAfterTimeoutWithDelay(3, 1000, this::shouldRetry, schedulers.network))
                .observeOn(schedulers.database)
                .map { tmdbShow ->
                    val show = showDao.getShowWithTmdbIdSync(tmdbShow.id) ?: TiviShow()
                    show.apply {
                        updateProperty(this::tmdbId, tmdbShow.id)
                        updateProperty(this::title, tmdbShow.name)
                        updateProperty(this::summary, tmdbShow.overview)
                        updateProperty(this::tmdbBackdropPath, tmdbShow.backdrop_path)
                        updateProperty(this::tmdbPosterPath, tmdbShow.poster_path)
                        updateProperty(this::homepage, tmdbShow.homepage)
                        lastTmdbUpdate = OffsetDateTime.now()
                    }
                    showDao.insertOrUpdateShow(show)
                }
                .doOnSubscribe { active += tmdbId }
                .doOnDispose { active -= tmdbId }
                .toCompletable()
    }

    private fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
        is HttpException -> throwable.code() == 429
        is IOException -> true
        is IllegalStateException -> true
        else -> false
    }
}
