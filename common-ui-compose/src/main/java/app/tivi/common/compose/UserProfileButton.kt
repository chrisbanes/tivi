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

package app.tivi.common.compose

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.tivi.data.entities.TraktUser
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun UserProfileButton(
    loggedIn: Boolean,
    user: TraktUser?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        when {
            loggedIn && user?.avatarUrl != null -> {
                CoilImage(
                    data = user.avatarUrl!!,
                    contentDescription = stringResource(R.string.cd_profile_pic, user.name),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            }
            loggedIn -> {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = stringResource(R.string.cd_user_profile)
                )
            }
            else -> {
                Icon(
                    painter = painterResource(R.drawable.ic_person_outline),
                    contentDescription = stringResource(R.string.cd_user_profile)
                )
            }
        }
    }
}
