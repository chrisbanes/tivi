// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.tivi.settings.Preference

@Composable
fun <T> Preference<T>.collectAsState() = flow.collectAsState(defaultValue)
