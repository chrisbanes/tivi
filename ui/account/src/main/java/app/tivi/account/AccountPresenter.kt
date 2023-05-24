/*
 * Copyright 2020 Google LLC
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

package app.tivi.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import app.tivi.data.traktauth.LoginToTraktInteractor
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.interactors.ClearUserDetails
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Parcelize
object AccountUiScreen : Screen

@Inject
class AccountUiPresenterFactory(
    private val presenterFactory: (AccountUiScreen, Navigator) -> AccountPresenter,
) : Presenter.Factory {
    override fun create(screen: Screen, navigator: Navigator, context: CircuitContext): Presenter<*>? {
        return when (screen) {
            is AccountUiScreen -> presenterFactory(screen, navigator)
            else -> null
        }
    }
}

@Inject
class AccountPresenter(
    @Assisted private val screen: AccountUiScreen,
    @Assisted private val navigator: Navigator,
    private val traktAuthRepository: TraktAuthRepository,
    private val loginToTraktInteractor: LoginToTraktInteractor,
    private val observeTraktAuthState: ObserveTraktAuthState,
    private val observeUserDetails: ObserveUserDetails,
    private val clearUserDetails: ClearUserDetails,
) : Presenter<AccountUiState> {

    @Composable
    override fun present(): AccountUiState {
        val user by observeUserDetails.flow.collectAsState(null)
        val authState by observeTraktAuthState.flow.collectAsState(TraktAuthState.LOGGED_OUT)
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            observeTraktAuthState(Unit)
            observeUserDetails(ObserveUserDetails.Params("me"))
        }

        return AccountUiState(
            user = user,
            authState = authState,
        ) { event ->
            when (event) {
                AccountUiEvent.NavigateToSettings -> {
                    // FIXME
                }
                AccountUiEvent.Login -> loginToTraktInteractor.launch()
                AccountUiEvent.Logout -> {
                    scope.launch {
                        traktAuthRepository.clearAuth()
                        clearUserDetails(ClearUserDetails.Params("me")).collect()
                    }
                }
            }
        }
    }
}
