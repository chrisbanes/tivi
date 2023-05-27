/*
 * Copyright 2023 Google LLC
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

package app.tivi.core.perf

import com.google.firebase.perf.ktx.trace as firebaseTrace
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidTracer : Tracer {
    override fun trace(name: String, block: () -> Unit) {
        try {
            firebaseTrace(name) {
                block()
            }
        } catch (e: IllegalStateException) {
            // Firebase likely isn't setup. Ignore the exception

            // TODO: intelligently disable Firebase Perf Monitoring if Firebase isn't setup
            // https://firebase.google.com/docs/perf-mon/disable-sdk?platform=android#disable-library
        }
    }
}
