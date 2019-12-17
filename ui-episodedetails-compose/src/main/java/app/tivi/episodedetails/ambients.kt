/*
 * Copyright 2019 Google LLC
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

package app.tivi.episodedetails

import androidx.compose.Ambient
import androidx.compose.Composable
import app.tivi.util.TiviDateFormatter

val TiviDateFormatterAmbient = Ambient.of<TiviDateFormatter>()

private typealias AmbientProvider = @Composable() (@Composable() () -> Unit) -> Unit

@Suppress("MoveLambdaOutsideParentheses")
fun WrapInAmbients(
    tiviDateFormatter: TiviDateFormatter,
    content: @Composable() () -> Unit
) {
    listOf<AmbientProvider>(
            { children ->
                TiviDateFormatterAmbient.Provider(value = tiviDateFormatter, children = children)
            }
    ).fold(content, { current, ambient -> { ambient(current) } }).invoke()
}
