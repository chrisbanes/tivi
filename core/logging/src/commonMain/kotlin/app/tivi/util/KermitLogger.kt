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

package app.tivi.util

import co.touchlab.kermit.Logger as Kermit
import co.touchlab.kermit.Severity
import me.tatarka.inject.annotations.Inject

@Inject
class KermitLogger : Logger {
    override fun setup(debugMode: Boolean) {
        Kermit.setMinSeverity(if (debugMode) Severity.Debug else Severity.Error)
    }

    override fun setUserId(id: String) {
        // no-op
    }

    override fun v(message: String, vararg args: Any?) {
        Kermit.v { message.format(args) }
    }

    override fun v(t: Throwable, message: String, vararg args: Any?) {
        Kermit.v(t) { message.format(args) }
    }

    override fun v(t: Throwable) {
        Kermit.v(t) { "" }
    }

    override fun d(message: String, vararg args: Any?) {
        Kermit.d { message.format(args) }
    }

    override fun d(t: Throwable, message: String, vararg args: Any?) {
        Kermit.d(t) { message.format(args) }
    }

    override fun d(t: Throwable) {
        Kermit.d(t) { "" }
    }

    override fun i(message: String, vararg args: Any?) {
        Kermit.i { message.format(args) }
    }

    override fun i(t: Throwable, message: String, vararg args: Any?) {
        Kermit.i(t) { message.format(args) }
    }

    override fun i(t: Throwable) {
        Kermit.i(t) { "" }
    }

    override fun w(message: String, vararg args: Any?) {
        Kermit.w { message.format(args) }
    }

    override fun w(t: Throwable, message: String, vararg args: Any?) {
        Kermit.w(t) { message.format(args) }
    }

    override fun w(t: Throwable) {
        Kermit.w(t) { "" }
    }

    override fun e(message: String, vararg args: Any?) {
        Kermit.e { message.format(args) }
    }

    override fun e(t: Throwable, message: String, vararg args: Any?) {
        Kermit.e(t) { message.format(args) }
    }

    override fun e(t: Throwable) {
        Kermit.e(t) { "" }
    }

    override fun wtf(message: String, vararg args: Any?) {
        Kermit.w { message.format(args) }
    }

    override fun wtf(t: Throwable, message: String, vararg args: Any?) {
        Kermit.w(t) { message.format(args) }
    }

    override fun wtf(t: Throwable) {
        Kermit.w(t) { "" }
    }
}
