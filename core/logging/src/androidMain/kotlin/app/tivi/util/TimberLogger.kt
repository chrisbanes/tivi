// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import me.tatarka.inject.annotations.Inject
import timber.log.Timber

@Inject
class TimberLogger : Logger {
    override fun setup(debugMode: Boolean) {
        if (debugMode) {
            Timber.plant(TiviDebugTree())
        }
        try {
            Timber.plant(CrashlyticsTree(FirebaseCrashlytics.getInstance()))
        } catch (e: IllegalStateException) {
            // Firebase is likely not setup in this project. Ignore the exception
        }
    }

    override fun setUserId(id: String) {
        try {
            FirebaseCrashlytics.getInstance().setUserId(id)
        } catch (e: IllegalStateException) {
            // Firebase is likely not setup in this project. Ignore the exception
        }
    }

    override fun v(throwable: Throwable?, message: () -> String) {
        Timber.v(throwable, message())
    }

    override fun d(throwable: Throwable?, message: () -> String) {
        Timber.d(throwable, message())
    }

    override fun i(throwable: Throwable?, message: () -> String) {
        Timber.i(throwable, message())
    }

    override fun e(throwable: Throwable?, message: () -> String) {
        Timber.e(throwable, message())
    }
}

/**
 * Special version of [Timber.DebugTree] which is tailored for Timber being wrapped
 * within another class.
 */
private class TiviDebugTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, createClassTag(), message, t)
    }

    private fun createClassTag(): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= CALL_STACK_INDEX) {
            throw IllegalStateException("Synthetic stacktrace didn't have enough elements: are you using proguard?")
        }
        var tag = stackTrace[CALL_STACK_INDEX].className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        tag = tag.substring(tag.lastIndexOf('.') + 1)
        // Tag length limit was removed in API 24.
        return when {
            Build.VERSION.SDK_INT >= 24 || tag.length <= MAX_TAG_LENGTH -> tag
            else -> tag.substring(0, MAX_TAG_LENGTH)
        }
    }

    companion object {
        private const val MAX_TAG_LENGTH = 23
        private const val CALL_STACK_INDEX = 7
        private val ANONYMOUS_CLASS by lazy { "(\\$\\d+)+$".toPattern() }
    }
}

private class CrashlyticsTree(
    private val firebaseCrashlytics: FirebaseCrashlytics,
) : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.INFO
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        firebaseCrashlytics.log(message)
        if (t != null) {
            firebaseCrashlytics.recordException(t)
        }
    }
}
