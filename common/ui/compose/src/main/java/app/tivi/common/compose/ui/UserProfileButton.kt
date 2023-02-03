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

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.models.TraktUser

@Composable
fun UserProfileButton(
    loggedIn: Boolean,
    user: TraktUser?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        when {
            loggedIn && user?.avatarUrl != null -> {
                AsyncImage(
                    model = user.avatarUrl!!,
                    requestBuilder = { crossfade(true) },
                    contentDescription = stringResource(
                        UiR.string.cd_profile_pic,
                        user.name ?: user.username,
                    ),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small),
                )
            }
            else -> {
                Icon(
                    imageVector = when {
                        loggedIn -> Icons.Default.Person
                        else -> Icons.Outlined.Person
                    },
                    contentDescription = stringResource(UiR.string.cd_user_profile),
                )
            }
        }
    }
}
