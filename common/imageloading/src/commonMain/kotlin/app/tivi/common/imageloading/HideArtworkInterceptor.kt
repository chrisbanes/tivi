// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.imagemodels.ImageModel
import app.tivi.data.models.TmdbImageEntity
import app.tivi.settings.TiviPreferences
import app.tivi.util.AppCoroutineDispatchers
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.size.pxOrElse
import coil3.toUri
import kotlin.random.Random
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class HideArtworkInterceptor(
  private val preferences: Lazy<TiviPreferences>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interceptor {
  override suspend fun intercept(chain: Interceptor.Chain): ImageResult = withContext(dispatchers.io) {
    when {
      preferences.value.developerHideArtwork.get() && isArtwork(chain.request.data) -> {
        val size = chain.request.sizeResolver.size()

        val placeholder = buildString {
          append("https://loremflickr.com/")
          append(size.width.pxOrElse { 300 })
          append('/')
          append(size.height.pxOrElse { 300 })
          append("/wildlife")
          append("?random=")
          append(Random.nextInt())
        }.toUri()

        chain
          .withRequest(
            chain.request.newBuilder()
              .data(placeholder)
              .build(),
          )
          .proceed()
      }

      else -> chain.proceed()
    }
  }

  private fun isArtwork(data: Any): Boolean = when (data) {
    is String -> data.contains("tmdb.org")
    is ImageModel -> true
    is TmdbImageEntity -> true
    else -> false
  }
}
