// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.tivi.data.traktauth.store.AuthStore
import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.launchOrThrow
import co.touchlab.kermit.Logger
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class TraktAuthRepository(
  private val scope: ApplicationCoroutineScope,
  private val dispatchers: AppCoroutineDispatchers,
  private val authStore: AuthStore,
  private val loginAction: Lazy<TraktLoginAction>,
  private val refreshTokenAction: Lazy<TraktRefreshTokenAction>,
  private val traktClient: Lazy<TraktClient>,
) {
  private val authState = MutableStateFlow<AuthState?>(null)
  private var authStateExpiry: Instant = Instant.DISTANT_PAST

  val state: Flow<TraktAuthState> = authState.map {
    when (it?.isAuthorized) {
      true -> TraktAuthState.LOGGED_IN
      else -> TraktAuthState.LOGGED_OUT
    }
  }

  fun isLoggedIn(): Boolean {
    return authState.value?.isAuthorized == true
  }

  private val logger by lazy { Logger.withTag("TraktAuthRepository") }

  init {
    // Read the auth state from the AuthStore
    scope.launchOrThrow {
      val state = getAuthState() ?: AuthState.Empty
      updateAuthState(authState = state, persist = false)
    }
  }

  suspend fun getAuthState(): AuthState? {
    val state = authState.value
    if (state != null && state.isAuthorized && Clock.System.now() < authStateExpiry) {
      logger.d { "getAuthState. Using cached tokens: $state" }
      return state
    }

    logger.d { "getAuthState. Retrieving tokens from AuthStore" }
    return withContext(dispatchers.io) { authStore.get() }
      ?.also { cacheAuthState(it) }
  }

  suspend fun login(): AuthState? {
    logger.d { "login()" }
    return loginAction.value().also {
      logger.d { "Login finished. Result: $it" }
      updateAuthState(authState = it ?: AuthState.Empty)
    }
  }

  suspend fun refreshTokens(): AuthState? {
    logger.d { "refreshTokens()" }
    return getAuthState()
      ?.let { currentState ->
        logger.d { "Calling refreshTokenAction with $currentState" }
        refreshTokenAction.value.invoke(currentState)
      }
      .also {
        logger.d { "refreshTokens finished. Result: $it" }
        updateAuthState(authState = it ?: AuthState.Empty)
      }
  }

  suspend fun logout() {
    updateAuthState(authState = AuthState.Empty)
  }

  private fun cacheAuthState(authState: AuthState) {
    this.authState.update { authState }
    authStateExpiry = when {
      authState.isAuthorized -> Clock.System.now() + 1.hours
      else -> Instant.DISTANT_PAST
    }
  }

  private suspend fun updateAuthState(authState: AuthState, persist: Boolean = true) {
    if (persist) {
      // Persist auth state
      withContext(dispatchers.io) {
        if (authState.isAuthorized) {
          authStore.save(authState)
          logger.d { "Saved state to AuthStore: $authState" }
        } else {
          authStore.clear()
          logger.d { "Cleared AuthStore" }
        }
      }
    }

    logger.d { "updateAuthState: $authState. Persist: $persist" }
    cacheAuthState(authState)

    logger.d { "updateAuthState: Clearing TraktClient auth tokens" }
    traktClient.value.invalidateAuthTokens()
  }
}

interface TraktClient {
  fun invalidateAuthTokens()
}
