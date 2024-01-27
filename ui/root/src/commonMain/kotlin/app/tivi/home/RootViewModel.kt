// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.interactors.LogoutTrakt
import app.tivi.domain.interactors.UpdateUserDetails
import app.tivi.domain.invoke
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.util.Logger
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class RootViewModel(
  @Assisted private val coroutineScope: CoroutineScope,
  observeTraktAuthState: Lazy<ObserveTraktAuthState>,
  private val updateUserDetails: Lazy<UpdateUserDetails>,
  observeUserDetails: Lazy<ObserveUserDetails>,
  private val logoutTrakt: Lazy<LogoutTrakt>,
  private val logger: Logger,
) {

  init {
    coroutineScope.launch {
      observeUserDetails.value.flow.collect { user ->
        logger.setUserId(user?.username ?: "")
      }
    }
    observeUserDetails.value.invoke(ObserveUserDetails.Params("me"))

    coroutineScope.launch {
      observeTraktAuthState.value.flow.collect { state ->
        if (state == TraktAuthState.LOGGED_IN) refreshMe()
      }
    }
    observeTraktAuthState.value.invoke(Unit)
  }

  private fun refreshMe() {
    coroutineScope.launch {
      try {
        updateUserDetails.value.invoke(UpdateUserDetails.Params("me", false))
      } catch (e: ResponseException) {
        if (e.response.status == HttpStatusCode.Unauthorized) {
          // If we got a 401 back from Trakt, we should clear out the auth state
          logoutTrakt.value.invoke()
        }
      } catch (ce: CancellationException) {
        throw ce
      } catch (t: Throwable) {
        // no-op
      }
    }
  }

  fun clear() {
    coroutineScope.cancel()
  }
}
