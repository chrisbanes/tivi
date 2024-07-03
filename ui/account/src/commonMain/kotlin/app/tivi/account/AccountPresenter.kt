// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.interactors.LoginTrakt
import app.tivi.domain.interactors.LogoutTrakt
import app.tivi.domain.invoke
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.screens.AccountScreen
import app.tivi.screens.SettingsScreen
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class AccountUiPresenterFactory(
  private val presenterFactory: (Navigator) -> AccountPresenter,
) : Presenter.Factory {
  override fun create(screen: Screen, navigator: Navigator, context: CircuitContext): Presenter<*>? {
    return when (screen) {
      is AccountScreen -> presenterFactory(navigator)
      else -> null
    }
  }
}

@Inject
class AccountPresenter(
  @Assisted private val navigator: Navigator,
  private val loginTrakt: Lazy<LoginTrakt>,
  private val logoutTrakt: Lazy<LogoutTrakt>,
  private val observeTraktAuthState: Lazy<ObserveTraktAuthState>,
  private val observeUserDetails: Lazy<ObserveUserDetails>,
) : Presenter<AccountUiState> {

  @Composable
  override fun present(): AccountUiState {
    val user by observeUserDetails.value.flow.collectAsRetainedState(null)
    val authState by observeTraktAuthState.value.flow
      .collectAsRetainedState(TraktAuthState.LOGGED_OUT)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
      observeTraktAuthState.value.invoke(Unit)
      observeUserDetails.value.invoke(ObserveUserDetails.Params("me"))
    }

    return AccountUiState(
      user = user,
      authState = authState,
    ) { event ->
      when (event) {
        AccountUiEvent.NavigateToSettings -> {
          navigator.pop() // dismiss ourselves
          navigator.goTo(SettingsScreen)
        }
        AccountUiEvent.Login -> scope.launch { loginTrakt.value.invoke() }
        AccountUiEvent.Logout -> scope.launch { logoutTrakt.value.invoke() }
      }
    }
  }
}
