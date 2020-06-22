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

import android.view.ViewGroup
import androidx.compose.Composable
import androidx.compose.Providers
import androidx.compose.Recomposer
import androidx.compose.getValue
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.clip
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.Spacer
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.preferredSize
import androidx.ui.layout.preferredSizeIn
import androidx.ui.layout.preferredWidth
import androidx.ui.livedata.observeAsState
import androidx.ui.material.Divider
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButton
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.TextButton
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Settings
import androidx.ui.res.stringResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import app.tivi.common.compose.ProvideInsets
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.VectorImage
import app.tivi.data.entities.TraktUser
import app.tivi.trakt.TraktAuthState
import app.tivi.util.TiviDateFormatter
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

fun composeAccountUi(
    viewGroup: ViewGroup,
    state: LiveData<AccountUiViewState>,
    insets: LiveData<WindowInsetsCompat>,
    actioner: (AccountUiAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter
): Any = viewGroup.setContent(Recomposer.current()) {
    MaterialThemeFromMdcTheme {
        Providers(TiviDateFormatterAmbient provides tiviDateFormatter) {
            ProvideInsets(insets) {
                val viewState by state.observeAsState()
                if (viewState != null) {
                    AccountUi(viewState!!, actioner)
                }
            }
        }
    }
}

@Composable
fun AccountUi(
    viewState: AccountUiViewState,
    actioner: (AccountUiAction) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.preferredHeight(16.dp))

        if (viewState.user != null) {
            UserRow(
                user = viewState.user,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.preferredHeight(16.dp))
        }

        Row(
            modifier = Modifier.gravity(Alignment.End)
                .padding(horizontal = 16.dp)
        ) {
            if (viewState.authState == TraktAuthState.LOGGED_OUT) {
                OutlinedButton(
                    onClick = { actioner(Login) },
                    text = { Text(text = stringResource(R.string.login)) }
                )
            } else {
                TextButton(
                    onClick = { actioner(Login) },
                    text = { Text(text = stringResource(R.string.refresh_credentials)) }
                )

                Spacer(modifier = Modifier.preferredWidth(8.dp))

                OutlinedButton(
                    onClick = { actioner(Logout) },
                    text = { Text(text = stringResource(R.string.logout)) }
                )
            }
        }

        Spacer(modifier = Modifier.preferredHeight(16.dp).fillMaxWidth())

        Divider()

        AppAction(
            label = stringResource(id = R.string.settings_title),
            icon = Icons.Default.Settings,
            onClick = { actioner(OpenSettings) }
        )

        Spacer(modifier = Modifier.preferredHeight(8.dp).fillMaxWidth())
    }
}

@Composable
private fun UserRow(
    user: TraktUser,
    modifier: Modifier = Modifier
) {
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        val avatarUrl = user.avatarUrl
        if (avatarUrl != null) {
            CoilImageWithCrossfade(
                data = avatarUrl,
                modifier = Modifier.preferredSize(40.dp)
                    .clip(RoundedCornerShape(50))
            )
        }

        Spacer(modifier = Modifier.preferredWidth(8.dp))

        Column {
            ProvideEmphasis(EmphasisAmbient.current.high) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.subtitle2
                )
            }

            ProvideEmphasis(EmphasisAmbient.current.medium) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
private fun AppAction(
    label: String,
    icon: VectorAsset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
            .preferredSizeIn(minHeight = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ProvideEmphasis(EmphasisAmbient.current.high) {
            Spacer(modifier = Modifier.preferredWidth(8.dp))

            VectorImage(vector = icon)

            Spacer(modifier = Modifier.preferredWidth(16.dp))

            ProvideEmphasis(EmphasisAmbient.current.high) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.body2
                )
            }
        }
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
