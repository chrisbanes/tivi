// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.interactors.LogoutTrakt
import app.tivi.domain.interactors.UpdateUserDetails
import app.tivi.domain.invoke
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.util.cancellableRunCatching
import app.tivi.util.launchOrThrow
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(FlowPreview::class)
@Inject
class RootViewModel(
  @Assisted private val coroutineScope: CoroutineScope,
  observeTraktAuthState: Lazy<ObserveTraktAuthState>,
  private val updateUserDetails: Lazy<UpdateUserDetails>,
  private val logoutTrakt: Lazy<LogoutTrakt>,
) {

  init {
    coroutineScope.launchOrThrow {
      observeTraktAuthState.value.flow
        .debounce(200.milliseconds)
        .filter { it == TraktAuthState.LOGGED_IN }
        .collect { refreshMe() }
    }
    observeTraktAuthState.value.invoke(Unit)
  }

  private fun refreshMe() {
    coroutineScope.launchOrThrow {
      cancellableRunCatching {
        updateUserDetails.value.invoke(UpdateUserDetails.Params("me", false))
      }.onFailure { e ->
        if (e is ResponseException && e.response.status == HttpStatusCode.Unauthorized) {
          // If we got a 401 back from Trakt, we should clear out the auth state
          logoutTrakt.value.invoke()
        }
      }
    }
  }

  fun clear() {
    coroutineScope.cancel()
  }
}
