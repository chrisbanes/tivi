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

package me.banes.chris.tivi.trakt

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
import org.threeten.bp.OffsetDateTime
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktShowFetcher @Inject constructor(
        private val showDao: TiviShowDao,
        private val trakt: TraktV2,
        private val schedulers: AppRxSchedulers
) {
    fun getShow(traktId: Int, entity: Show? = null): Maybe<TiviShow> {
        val dbSource = showDao.getShowWithTraktIdMaybe(traktId)
                .subscribeOn(schedulers.database)

        val fromEntity = entity?.let { appendRx(Maybe.just(entity)) } ?: Maybe.empty<TiviShow>()

        val networkSource = appendRx(
                trakt.shows().summary(traktId.toString(), Extended.NOSEASONS).toRxMaybe()
                        .subscribeOn(schedulers.network)
                        .retryWhen(RetryAfterTimeoutWithDelay(3, 1000, this::shouldRetry, schedulers.network))
        )

        return Maybe.concat(dbSource, fromEntity, networkSource).firstElement()
    }

    private fun appendRx(maybe: Maybe<Show>): Maybe<TiviShow> {
        return maybe.observeOn(schedulers.database)
                .map(this::upsertShow)
                .map(TiviShow::traktId)
                .flatMap(showDao::getShowWithTraktIdMaybe)
    }

    fun updateShow(traktId: Int): Single<TiviShow> {
        return trakt.shows().summary(traktId.toString(), Extended.FULL).toRxSingle()
                .subscribeOn(schedulers.network)
                .observeOn(schedulers.database)
                .map(this::upsertShow)
    }

    private fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
        is HttpException -> throwable.code() == 429
        is IOException -> true
        else -> false
    }

    private fun upsertShow(traktShow: Show) : TiviShow {
        val show = showDao.getShowWithTraktIdSync(traktShow.ids.trakt) ?: TiviShow()
        show.apply {
            updateProperty(this::traktId, traktShow.ids.trakt)
            updateProperty(this::tmdbId, traktShow.ids.tmdb)
            updateProperty(this::title, traktShow.title)
            updateProperty(this::summary, traktShow.overview)
            updateProperty(this::homepage, traktShow.homepage)
            updateProperty(this::rating, traktShow.rating?.toFloat())
            lastTraktUpdate = OffsetDateTime.now()
        }
        return showDao.insertOrUpdateShow(show)
    }
}