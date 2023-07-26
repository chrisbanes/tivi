// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

internal fun CompositeLogger(vararg loggers: Logger): Logger = CompositeLogger(loggers = loggers)

private class CompositeLogger(
    private val loggers: Array<out Logger>,
) : Logger {
    override fun setup(debugMode: Boolean) {
        loggers.forEach { it.setup(debugMode) }
    }

    override fun setUserId(id: String) {
        loggers.forEach { it.setUserId(id) }
    }

    override fun v(throwable: Throwable?, message: () -> String) {
        loggers.forEach { it.v(throwable, message) }
    }

    override fun d(throwable: Throwable?, message: () -> String) {
        loggers.forEach { it.d(throwable, message) }
    }

    override fun i(throwable: Throwable?, message: () -> String) {
        loggers.forEach { it.i(throwable, message) }
    }

    override fun e(throwable: Throwable?, message: () -> String) {
        loggers.forEach { it.e(throwable, message) }
    }

    override fun w(throwable: Throwable?, message: () -> String) {
        loggers.forEach { it.w(throwable, message) }
    }
}
