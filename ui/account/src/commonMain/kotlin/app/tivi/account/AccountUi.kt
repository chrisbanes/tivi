// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.data.models.TraktUser
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.overlays.LocalNavigator
import app.tivi.screens.AccountScreen
import app.tivi.screens.SettingsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Inject
class AccountUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is AccountScreen -> {
            ui<AccountUiState> { state, modifier ->
                AccountUi(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun AccountUi(
    state: AccountUiState,
    modifier: Modifier = Modifier,
) {
    // Need to extract the eventSink out to a local val, so that the Compose Compiler
    // treats it as stable. See: https://issuetracker.google.com/issues/256100927
    val eventSink = state.eventSink

    val navigator = LocalNavigator.current

    AccountUi(
        viewState = state,
        openSettings = {
            // Really we should send up the NavigateToSettings event to the presenter, and let
            // it handle the navigation. Due to how this UI is presented (in an overlay), the
            // navigator given to the presenter is a no-op. To workaround that, we stuff the actual
            // navigator used into a composition local, and then manually call it.
            // eventSink(AccountUiEvent.NavigateToSettings)
            navigator.goTo(SettingsScreen)
        },
        login = { eventSink(AccountUiEvent.Login) },
        logout = { eventSink(AccountUiEvent.Logout) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountUi(
    viewState: AccountUiState,
    openSettings: () -> Unit,
    login: () -> Unit,
    logout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))

        if (viewState.user != null) {
            UserRow(
                user = viewState.user,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .wrapContentSize(Alignment.CenterEnd)
                .align(Alignment.End),
        ) {
            if (viewState.authState == TraktAuthState.LOGGED_OUT) {
                OutlinedButton(onClick = login) {
                    Text(text = strings.login)
                }
            } else {
                TextButton(onClick = login) {
                    Text(text = strings.refreshCredentials)
                }
            }

            OutlinedButton(onClick = logout) {
                Text(text = strings.logout)
            }
        }

        Spacer(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(),
        )

        Divider()

        AppAction(
            label = strings.settingsTitle,
            icon = Icons.Default.Settings,
            contentDescription = strings.settingsTitle,
            onClick = openSettings,
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun UserRow(
    user: TraktUser,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        val avatarUrl = user.avatarUrl
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = strings.cdProfilePic(user.name ?: user.username),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50)),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = user.name ?: strings.accountNameUnknown,
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = user.username,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AppAction(
    label: String,
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// @Preview
@Composable
fun PreviewUserRow() {
    UserRow(
        TraktUser(
            id = 0,
            username = "sammendes",
            name = "Sam Mendes",
            location = "London, UK",
            joined = LocalDateTime(
                year = 2019,
                monthNumber = 5,
                dayOfMonth = 4,
                hour = 11,
                minute = 12,
                second = 33,
                nanosecond = 0,
            ).toInstant(TimeZone.currentSystemDefault()),
        ),
    )
}
