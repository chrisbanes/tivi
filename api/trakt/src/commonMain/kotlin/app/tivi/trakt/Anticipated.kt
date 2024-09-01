// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.trakt

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.model.TraktShow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class TraktShowsApiExtra(private val client: HttpClient) {

  suspend fun getAnticipated(
    page: Int,
    limit: Int,
    extended: TraktExtended? = null,
  ): List<TraktAnticipatedShow> = client.get("shows/anticipated") {
    parameter("page", page)
    parameter("limit", limit)
    extended?.let { parameter("extended", it.value) }
  }.body()
}

@Serializable
data class TraktAnticipatedShow(
  @SerialName("show") val show: TraktShow? = null,
  @SerialName("list_count") val listCount: Int? = null,
)
