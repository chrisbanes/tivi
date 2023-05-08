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

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

@Composable
fun stringResource(resource: StringResource): String =
    StringDesc.Resource(resource).toString(LocalContext.current)

@Composable
fun stringResource(resource: StringResource, vararg args: Any): String =
    StringDesc.ResourceFormatted(resource, *args).toString(LocalContext.current)

@Composable
fun stringResource(resource: PluralsResource, quantity: Int): String =
    StringDesc.Plural(resource, quantity).toString(LocalContext.current)

@Composable
fun stringResource(resource: PluralsResource, quantity: Int, vararg args: Any): String =
    StringDesc.PluralFormatted(resource, quantity, *args).toString(LocalContext.current)
