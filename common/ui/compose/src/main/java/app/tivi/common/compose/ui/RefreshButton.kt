/*
 * Copyright 2021 Google LLC
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

package app.tivi.common.compose.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tivi.common.ui.resources.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun RefreshButton(
    refreshing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = !refreshing,
        modifier = modifier,
    ) {
        Crossfade(refreshing) { targetRefreshing ->
            if (targetRefreshing) {
                AutoSizedCircularProgressIndicator(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(MR.strings.cd_refresh),
                )
            }
        }
    }
}

@Composable
fun RefreshButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = stringResource(MR.strings.cd_refresh),
        )
    }
}
