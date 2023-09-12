// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.licenses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.tivi.data.licenses.LicenseItem
import app.tivi.domain.interactors.FetchLicensesList
import app.tivi.screens.LicensesScreen
import app.tivi.screens.UrlScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class LicensesUiPresenterFactory(
    private val presenterFactory: (Navigator) -> LicensesPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? = when (screen) {
        is LicensesScreen -> presenterFactory(navigator)
        else -> null
    }
}

@Inject
class LicensesPresenter(
    @Assisted private val navigator: Navigator,
    private val fetchLicensesList: FetchLicensesList,
) : Presenter<LicensesUiState> {

    @Composable
    override fun present(): LicensesUiState {
        var licenseItemList by remember { mutableStateOf(emptyList<LicenseItem>()) }

        LaunchedEffect(Unit) {
            val openSourceList = fetchLicensesList(Unit)
            licenseItemList = openSourceList.getOrDefault(emptyList())
        }

        fun eventSink(event: LicensesUiEvent) {
            when (event) {
                LicensesUiEvent.NavigateUp -> navigator.pop()
                is LicensesUiEvent.NavigateRepository -> navigator.goTo(UrlScreen(event.url))
            }
        }

        return LicensesUiState(
            licenseItemList = licenseItemList,
            eventSink = ::eventSink,
        )
    }
}
