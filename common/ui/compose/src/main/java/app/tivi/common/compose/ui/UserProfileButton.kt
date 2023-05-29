// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
import androidx.compose.ui.unit.dp
import app.tivi.common.ui.resources.MR
import app.tivi.data.models.TraktUser
import dev.icerock.moko.resources.compose.stringResource

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
                        MR.strings.cd_profile_pic,
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
                    contentDescription = stringResource(MR.strings.cd_user_profile),
                )
            }
        }
    }
}
