// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.executeSync
import app.tivi.domain.interactors.ClearTraktAuthState
import app.tivi.domain.interactors.UpdateUserDetails
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.util.Logger
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class MainActivityViewModel(
    observeTraktAuthState: ObserveTraktAuthState,
    private val updateUserDetails: UpdateUserDetails,
    observeUserDetails: ObserveUserDetails,
    private val clearTraktAuthState: ClearTraktAuthState,
    private val logger: Logger,
) : ViewModel() {
    init {
        viewModelScope.launch {
            observeUserDetails.flow.collect { user ->
                logger.setUserId(user?.username ?: "")
            }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        viewModelScope.launch {
            observeTraktAuthState.flow.collect { state ->
                if (state == TraktAuthState.LOGGED_IN) refreshMe()
            }
        }
        observeTraktAuthState(Unit)
    }

    private fun refreshMe() {
        viewModelScope.launch {
            try {
                updateUserDetails(UpdateUserDetails.Params("me", false))
            } catch (e: ResponseException) {
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    // If we got a 401 back from Trakt, we should clear out the auth state
                    clearTraktAuthState.executeSync()
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                // no-op
            }
        }
    }
}
