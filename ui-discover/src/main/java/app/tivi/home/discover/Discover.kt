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

package app.tivi.home.discover

import android.view.ViewGroup
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.ui.tooling.preview.Preview
import app.tivi.common.compose.IconResource
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.ProvideDisplayInsets
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.statusBarsPadding
import app.tivi.data.entities.TraktUser
import app.tivi.trakt.TraktAuthState
import app.tivi.util.TiviDateFormatter
import com.google.android.material.composethemeadapter.MdcTheme
import dev.chrisbanes.accompanist.coil.CoilImage

internal fun ViewGroup.composeDiscover(
    state: LiveData<DiscoverViewState>,
    actioner: (DiscoverAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter,
    textCreator: DiscoverTextCreator
): Any = setContent(Recomposer.current()) {
    Providers(
        TiviDateFormatterAmbient provides tiviDateFormatter,
        DiscoverTextCreatorAmbient provides textCreator
    ) {
        MdcTheme {
            LogCompositions("MdcTheme")

            ProvideDisplayInsets {
                LogCompositions("ProvideInsets")
                val viewState by state.observeAsState()
                if (viewState != null) {
                    LogCompositions("ViewState observeAsState")
                    Discover(viewState!!, actioner)
                }
            }
        }
    }
}

@Composable
fun Discover(
    state: DiscoverViewState,
    actioner: (DiscoverAction) -> Unit
) {
    Scaffold(
        topBar = {
            DiscoverAppBar(
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                actioner = actioner,
                modifier = Modifier.statusBarsPadding().fillMaxWidth()
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
    }
}

@Composable
private fun DiscoverAppBar(
    loggedIn: Boolean,
    user: TraktUser?,
    actioner: (DiscoverAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.discover_title),
                style = MaterialTheme.typography.h6
            )
        },
        actions = {
            IconButton(onClick = { actioner(OpenUserDetails) }) {
                when {
                    loggedIn && user?.avatarUrl != null -> {
                        CoilImage(
                            data = user.avatarUrl!!,
                            modifier = Modifier.preferredSize(32.dp).clip(CircleShape)
                        )
                    }
                    loggedIn -> IconResource(R.drawable.ic_person)
                    else -> IconResource(R.drawable.ic_person_outline)
                }
            }
        },
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.93f),
        modifier = modifier
    )
}

@Preview
@Composable
private fun PreviewDiscoverAppBar() {
    DiscoverAppBar(
        loggedIn = false,
        user = null,
        actioner = {}
    )
}
