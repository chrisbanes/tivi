// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.licenses

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import app.tivi.domain.interactors.FetchLicensesList
import app.tivi.screens.LicensesScreen
import app.tivi.screens.UrlScreen
import app.tivi.util.AppCoroutineDispatchers
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.withContext
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
  private val fetchLicensesList: Lazy<FetchLicensesList>,
  private val dispatchers: AppCoroutineDispatchers,
) : Presenter<LicensesUiState> {

  @Composable
  override fun present(): LicensesUiState {
    val licenseItemList by produceState(emptyList()) {
      value = withContext(dispatchers.io) {
        fetchLicensesList.value.invoke(Unit)
          .getOrDefault(emptyList())
          .groupBy { it.groupId }
          .map { (groupId, artifacts) ->
            LicenseGroup(
              id = groupId,
              artifacts = artifacts.sortedBy { it.artifactId },
            )
          }
          .sortedBy { it.id }
      }
    }

    fun eventSink(event: LicensesUiEvent) {
      when (event) {
        LicensesUiEvent.NavigateUp -> navigator.pop()
        is LicensesUiEvent.NavigateRepository -> {
          val url = event.artifact.scm?.url
          if (!url.isNullOrEmpty()) {
            navigator.goTo(UrlScreen(url))
          }
        }
      }
    }

    return LicensesUiState(
      licenses = licenseItemList,
      eventSink = ::eventSink,
    )
  }
}
