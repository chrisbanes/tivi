// Copyright 2023, Christopher Banes and the Tivi project contributors
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

    override fun v(throwable: Throwable?, message: () -> String) {
        if (throwable != null) {
            Kermit.v(throwable = throwable, message = message)
        } else {
            Kermit.v(message = message)
        }
    }

    override fun d(throwable: Throwable?, message: () -> String) {
        if (throwable != null) {
            Kermit.d(throwable = throwable, message = message)
        } else {
            Kermit.d(message = message)
        }
    }

    override fun i(throwable: Throwable?, message: () -> String) {
        if (throwable != null) {
            Kermit.i(throwable = throwable, message = message)
        } else {
            Kermit.i(message = message)
        }
    }

    override fun e(throwable: Throwable?, message: () -> String) {
        if (throwable != null) {
            Kermit.e(throwable = throwable, message = message)
        } else {
            Kermit.e(message = message)
        }
    }
}
