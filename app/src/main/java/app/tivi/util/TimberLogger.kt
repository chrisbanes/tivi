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

package app.tivi.util

import com.crashlytics.android.Crashlytics
import timber.log.Timber
import javax.inject.Inject

class TimberLogger @Inject constructor() : Logger {
    fun setup(debugMode: Boolean) {
        if (debugMode) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun v(message: String, vararg args: Any) = Timber.v(message, args)

    override fun v(t: Throwable, message: String, vararg args: Any) = Timber.v(t, message, args)

    override fun v(t: Throwable) = Timber.v(t)

    override fun d(message: String, vararg args: Any) = Timber.d(message, args)

    override fun d(t: Throwable, message: String, vararg args: Any) = Timber.d(t, message, args)

    override fun d(t: Throwable) = Timber.d(t)

    override fun i(message: String, vararg args: Any) = Timber.i(message, args)

    override fun i(t: Throwable, message: String, vararg args: Any) = Timber.i(t, message, args)

    override fun i(t: Throwable) = Timber.i(t)

    override fun w(message: String, vararg args: Any) = Timber.w(message, args)

    override fun w(t: Throwable, message: String, vararg args: Any) = Timber.w(t, message, args)

    override fun w(t: Throwable) = Timber.w(t)

    override fun e(message: String, vararg args: Any) = Timber.e(message, args)

    override fun e(t: Throwable, message: String, vararg args: Any) = Timber.e(t, message, args)

    override fun e(t: Throwable) = Timber.e(t)

    override fun wtf(message: String, vararg args: Any) = Timber.wtf(message, args)

    override fun wtf(t: Throwable, message: String, vararg args: Any) = Timber.wtf(t, message, args)

    override fun wtf(t: Throwable) = Timber.wtf(t)

    override fun logForCrash(message: String) {
        Crashlytics.log(message)
    }

    override fun logForCrash(message: String, vararg args: Any) {
        if (args.isNotEmpty()) {
            Crashlytics.log(message.format(args))
        } else {
            logForCrash(message)
        }
    }
}