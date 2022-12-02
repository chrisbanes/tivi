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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.data.entities.TraktUser
import app.tivi.trakt.TraktAuthState
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import app.tivi.common.ui.resources.R as UiR

@Composable
fun AccountUi(
    openSettings: () -> Unit
) {
    AccountUi(
        viewModel = hiltViewModel(),
        openSettings = openSettings
    )
}

@Composable
internal fun AccountUi(
    viewModel: AccountUiViewModel,
    openSettings: () -> Unit
) {
    val viewState by viewModel.state.collectAsState()

    val loginLauncher = rememberLauncherForActivityResult(
        viewModel.buildLoginActivityResult()
    ) { result ->
        if (result != null) {
            viewModel.onLoginResult(result)
        }
    }

    AccountUi(
        viewState = viewState,
        openSettings = openSettings,
        login = { loginLauncher.launch(Unit) },
        logout = { viewModel.logout() }
    )
}

@Composable
internal fun AccountUi(
    viewState: AccountUiViewState,
    openSettings: () -> Unit,
    login: () -> Unit,
    logout: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.defaultMinSize(minHeight = 200.dp)
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))

            if (viewState.user != null) {
                UserRow(
                    user = viewState.user,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            FlowRow(
                mainAxisAlignment = FlowMainAxisAlignment.End,
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 4.dp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .wrapContentSize(Alignment.CenterEnd)
                    .align(Alignment.End)
            ) {
                if (viewState.authState == TraktAuthState.LOGGED_OUT) {
                    OutlinedButton(onClick = login) {
                        Text(text = stringResource(UiR.string.login))
                    }
                } else {
                    TextButton(onClick = login) {
                        Text(text = stringResource(UiR.string.refresh_credentials))
                    }
                }

                OutlinedButton(onClick = logout) {
                    Text(text = stringResource(UiR.string.logout))
                }
            }

            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth()
            )

            Divider()

            AppAction(
                label = stringResource(UiR.string.settings_title),
                icon = Icons.Default.Settings,
                contentDescription = stringResource(UiR.string.settings_title),
                onClick = openSettings
            )

            Spacer(
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UserRow(
    user: TraktUser,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        val avatarUrl = user.avatarUrl
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                requestBuilder = { crossfade(true) },
                contentDescription = stringResource(
                    UiR.string.cd_profile_pic,
                    user.name
                        ?: user.username
                ),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = user.name ?: stringResource(UiR.string.account_name_unknown),
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = user.username,
                style = MaterialTheme.typography.bodySmall
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
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
fun PreviewUserRow() {
    UserRow(
        TraktUser(
            id = 0,
            username = "sammendes",
            name = "Sam Mendes",
            location = "London, UK",
            joined = OffsetDateTime.of(2019, 5, 4, 11, 12, 33, 0, ZoneOffset.UTC)
        )
    )
}
