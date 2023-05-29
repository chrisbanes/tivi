// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
        Kermit.v { message.format(*args) }
    }

    override fun v(t: Throwable, message: String, vararg args: Any?) {
        Kermit.v(t) { message.format(*args) }
    }

    override fun v(t: Throwable) {
        Kermit.v(t) { "" }
    }

    override fun d(message: String, vararg args: Any?) {
        Kermit.d { message.format(*args) }
    }

    override fun d(t: Throwable, message: String, vararg args: Any?) {
        Kermit.d(t) { message.format(*args) }
    }

    override fun d(t: Throwable) {
        Kermit.d(t) { "" }
    }

    override fun i(message: String, vararg args: Any?) {
        Kermit.i { message.format(*args) }
    }

    override fun i(t: Throwable, message: String, vararg args: Any?) {
        Kermit.i(t) { message.format(*args) }
    }

    override fun i(t: Throwable) {
        Kermit.i(t) { "" }
    }

    override fun w(message: String, vararg args: Any?) {
        Kermit.w { message.format(*args) }
    }

    override fun w(t: Throwable, message: String, vararg args: Any?) {
        Kermit.w(t) { message.format(*args) }
    }

    override fun w(t: Throwable) {
        Kermit.w(t) { "" }
    }

    override fun e(message: String, vararg args: Any?) {
        Kermit.e { message.format(*args) }
    }

    override fun e(t: Throwable, message: String, vararg args: Any?) {
        Kermit.e(t) { message.format(*args) }
    }

    override fun e(t: Throwable) {
        Kermit.e(t) { "" }
    }

    override fun wtf(message: String, vararg args: Any?) {
        Kermit.w { message.format(*args) }
    }

    override fun wtf(t: Throwable, message: String, vararg args: Any?) {
        Kermit.w(t) { message.format(*args) }
    }

    override fun wtf(t: Throwable) {
        Kermit.w(t) { "" }
    }
}
